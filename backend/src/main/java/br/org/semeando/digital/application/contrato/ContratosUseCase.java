package br.org.semeando.digital.application.contrato;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.AcessoNegadoException;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.contrato.ContratoPrestacaoServicos;
import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.DocumentoVinculoRepository;
import br.org.semeando.digital.domain.contrato.FichaSocio;
import br.org.semeando.digital.domain.contrato.GeradorDocumentoVinculo;
import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** RF-13 a RF-18: geração, validação, assinatura, versionamento e arquivo de contratos/fichas de sócio. */
@CasoDeUso
public class ContratosUseCase {

    private final DocumentoVinculoRepository vinculos;
    private final MatriculaRepository matriculas;
    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final GeradorDocumentoVinculo gerador;
    private final Auditor auditor;
    private final Clock clock;

    public ContratosUseCase(DocumentoVinculoRepository vinculos, MatriculaRepository matriculas,
            AlunoRepository alunos, ResponsavelRepository responsaveis, GeradorDocumentoVinculo gerador,
            Auditor auditor, Clock clock) {
        this.vinculos = vinculos;
        this.matriculas = matriculas;
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.gerador = gerador;
        this.auditor = auditor;
        this.clock = clock;
    }

    public DocumentoVinculo buscar(UUID id, Ator ator) {
        DocumentoVinculo documento = vinculos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato", id));
        garantirAcesso(documento, ator);
        return documento;
    }

    /**
     * RF-17: gera o vínculo com valores propostos pela secretaria. Divergência da tabela oficial
     * bloqueia a geração (RN-02). Se já existir vínculo vigente, este passa a ser uma nova versão (RF-16).
     */
    public DocumentoVinculo gerar(UUID matriculaId, Dinheiro valorParcela, int numeroParcelas, Ator ator) {
        Matricula matricula = matriculas.buscarPorId(matriculaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Matrícula", matriculaId));
        Aluno aluno = aluno(matricula.alunoId());
        Responsavel responsavel = responsavel(aluno);
        DocumentoVinculo novo = gerador.gerar(aluno, responsavel, matricula, valorParcela, numeroParcelas,
                ator.nome(), clock.instant());
        return vinculos.buscarVigentePorMatricula(matriculaId)
                .map(atual -> substituir(atual, novo.conteudo(), ator))
                .orElseGet(() -> {
                    auditor.registrar(ator, AcaoAuditada.CRIACAO, "Contrato", novo.id(), novo.tipo().name());
                    return vinculos.salvar(novo);
                });
    }

    /** RF-16: regera o documento com os dados atuais do aluno (ex.: novos autorizados). */
    public DocumentoVinculo gerarNovaVersao(UUID id, Ator ator) {
        DocumentoVinculo atual = vinculos.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contrato", id));
        Matricula matricula = matriculas.buscarPorId(atual.matriculaId()).orElseThrow();
        Aluno aluno = aluno(atual.alunoId());
        DocumentoVinculo regenerado = gerador.gerarComTabelaOficial(aluno, responsavel(aluno), matricula,
                ator.nome(), clock.instant());
        return substituir(atual, regenerado.conteudo(), ator);
    }

    /** RF-15: assinatura eletrônica com IP e hash. Responsáveis só assinam documentos dos próprios filhos. */
    public DocumentoVinculo assinar(UUID id, String enderecoIp, Ator ator) {
        DocumentoVinculo documento = buscar(id, ator);
        documento.assinar(ator.nome(), enderecoIp, clock.instant());
        auditor.registrar(ator, AcaoAuditada.ASSINATURA, "Contrato", id,
                "IP " + enderecoIp + " hash " + documento.hashDocumento());
        return vinculos.salvar(documento);
    }

    public List<DocumentoVinculo> versoes(UUID id, Ator ator) {
        DocumentoVinculo documento = buscar(id, ator);
        return vinculos.listarVersoes(documento.numero()).stream()
                .sorted(Comparator.comparingInt(DocumentoVinculo::versao).reversed())
                .toList();
    }

    /** RF-18: repositório pesquisável por aluno, turma ou ano letivo. */
    public List<DocumentoVinculo> pesquisar(UUID alunoId, UUID turmaId, Integer anoLetivo,
            StatusDocumentoVinculo status) {
        List<DocumentoVinculo> encontrados = vinculos.pesquisar(alunoId, anoLetivo, status);
        if (turmaId == null) {
            return encontrados;
        }
        Set<UUID> matriculasDaTurma = matriculas.listar(null, anoLetivo).stream()
                .filter(m -> m.turmaId().equals(turmaId)).map(Matricula::id).collect(Collectors.toSet());
        return encontrados.stream().filter(d -> matriculasDaTurma.contains(d.matriculaId())).toList();
    }

    private DocumentoVinculo substituir(DocumentoVinculo atual, String conteudo, Ator ator) {
        DocumentoVinculo proxima = switch (atual) {
            case ContratoPrestacaoServicos contrato -> contrato.novaVersao(conteudo, ator.nome(), clock.instant());
            case FichaSocio ficha -> ficha.novaVersao(conteudo, ator.nome(), clock.instant());
        };
        atual.marcarComoSubstituido();
        vinculos.salvar(atual);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Contrato", proxima.id(),
                "Versão " + proxima.versao() + " substitui " + atual.id());
        return vinculos.salvar(proxima);
    }

    private void garantirAcesso(DocumentoVinculo documento, Ator ator) {
        if (!ator.responsavel()) {
            return;
        }
        Aluno aluno = aluno(documento.alunoId());
        if (ator.cpf() == null || !aluno.cpfResponsavel().equals(ator.cpf())) {
            throw new AcessoNegadoException("Documento não pertence a um aluno sob sua responsabilidade");
        }
    }

    private Aluno aluno(UUID id) {
        return alunos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
    }

    private Responsavel responsavel(Aluno aluno) {
        return responsaveis.buscarPorId(aluno.responsavelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Responsável", aluno.responsavelId()));
    }
}
