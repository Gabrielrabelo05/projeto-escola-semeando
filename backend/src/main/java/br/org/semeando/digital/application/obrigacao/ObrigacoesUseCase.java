package br.org.semeando.digital.application.obrigacao;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.port.ArmazenamentoArquivos;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.obrigacao.Comprovante;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRegulatoria;
import br.org.semeando.digital.domain.obrigacao.ObrigacaoRepository;
import br.org.semeando.digital.domain.obrigacao.StatusObrigacao;
import br.org.semeando.digital.domain.obrigacao.TipoObrigacao;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** RF-47, RF-48, RF-53 e RF-54. */
@CasoDeUso
public class ObrigacoesUseCase {

    private final ObrigacaoRepository obrigacoes;
    private final ArmazenamentoArquivos arquivos;
    private final Auditor auditor;
    private final Clock clock;

    public ObrigacoesUseCase(ObrigacaoRepository obrigacoes, ArmazenamentoArquivos arquivos, Auditor auditor,
            Clock clock) {
        this.obrigacoes = obrigacoes;
        this.arquivos = arquivos;
        this.auditor = auditor;
        this.clock = clock;
    }

    /** RF-47 (painel) / RF-54 (histórico por ano). */
    public List<ObrigacaoRegulatoria> listar(Integer ano, StatusObrigacao status) {
        return obrigacoes.listar(ano, status).stream()
                .sorted(Comparator.comparing(ObrigacaoRegulatoria::prazo))
                .toList();
    }

    public ObrigacaoRegulatoria buscar(UUID id) {
        return obrigacoes.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Obrigação", id));
    }

    public ObrigacaoRegulatoria criar(TipoObrigacao tipo, int ano, Integer bimestre, String responsavel, Ator ator) {
        if (obrigacoes.existe(tipo, ano, bimestre)) {
            throw new DomainException("DUPLICADO", "Obrigação já cadastrada para o período");
        }
        ObrigacaoRegulatoria obrigacao = obrigacoes.salvar(
                ObrigacaoRegulatoria.criar(tipo, ano, bimestre, responsavel));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Obrigacao", obrigacao.id(), obrigacao.nome());
        return obrigacao;
    }

    public ObrigacaoRegulatoria criarPersonalizada(String nome, LocalDate prazo, String responsavel,
            List<String> checklist, Ator ator) {
        ObrigacaoRegulatoria obrigacao = obrigacoes.salvar(
                ObrigacaoRegulatoria.criarPersonalizada(nome, prazo, responsavel, checklist));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Obrigacao", obrigacao.id(), nome);
        return obrigacao;
    }

    /** Cria as obrigações legais do ano (SMTT, Censo e os 4 bimestres do Sistema Presença). */
    public List<ObrigacaoRegulatoria> gerarCalendarioAnual(int ano, String responsavel, Ator ator) {
        criarSeAusente(TipoObrigacao.SMTT, ano, null, responsavel, ator);
        criarSeAusente(TipoObrigacao.CENSO_ESCOLAR, ano, null, responsavel, ator);
        for (int bimestre = 1; bimestre <= 4; bimestre++) {
            criarSeAusente(TipoObrigacao.SISTEMA_PRESENCA, ano, bimestre, responsavel, ator);
        }
        return listar(ano, null);
    }

    public ObrigacaoRegulatoria marcarItem(UUID id, int indice, boolean concluido, Ator ator) {
        ObrigacaoRegulatoria obrigacao = buscar(id);
        obrigacao.marcarItem(indice, concluido);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Obrigacao", id, "Checklist item " + indice + "=" + concluido);
        return obrigacoes.salvar(obrigacao);
    }

    /** RF-53: upload do comprovante de entrega. */
    public ObrigacaoRegulatoria anexarComprovante(UUID id, String nomeArquivo, byte[] conteudo, Ator ator) {
        ObrigacaoRegulatoria obrigacao = buscar(id);
        String caminho = arquivos.salvar("obrigacoes/" + id, nomeArquivo, conteudo);
        obrigacao.anexarComprovante(new Comprovante(nomeArquivo, caminho, clock.instant(), ator.nome()));
        auditor.registrar(ator, AcaoAuditada.UPLOAD, "Obrigacao", id, nomeArquivo);
        return obrigacoes.salvar(obrigacao);
    }

    public ObrigacaoRegulatoria concluir(UUID id, Ator ator) {
        ObrigacaoRegulatoria obrigacao = buscar(id);
        obrigacao.concluir(LocalDate.now(clock));
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Obrigacao", id, "Concluída");
        return obrigacoes.salvar(obrigacao);
    }

    public byte[] baixarComprovante(UUID id, int indice) {
        ObrigacaoRegulatoria obrigacao = buscar(id);
        if (indice < 0 || indice >= obrigacao.comprovantes().size()) {
            throw new RecursoNaoEncontradoException("Comprovante", indice);
        }
        return arquivos.ler(obrigacao.comprovantes().get(indice).caminho());
    }

    private void criarSeAusente(TipoObrigacao tipo, int ano, Integer bimestre, String responsavel, Ator ator) {
        if (!obrigacoes.existe(tipo, ano, bimestre)) {
            criar(tipo, ano, bimestre, responsavel, ator);
        }
    }
}
