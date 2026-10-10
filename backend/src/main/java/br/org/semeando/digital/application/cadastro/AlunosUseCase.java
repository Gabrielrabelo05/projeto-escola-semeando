package br.org.semeando.digital.application.cadastro;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.auth.EmissorConvite;
import br.org.semeando.digital.application.auth.EmissorConvite.Convite;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.aluno.AtividadeExtracurricular;
import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** RF-07 / RF-31 / RF-37: ficha completa do aluno. */
@CasoDeUso
public class AlunosUseCase {

    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final EmissorConvite convites;
    private final Auditor auditor;

    public AlunosUseCase(AlunoRepository alunos, ResponsavelRepository responsaveis, EmissorConvite convites,
            Auditor auditor) {
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.convites = convites;
        this.auditor = auditor;
    }

    public List<Aluno> listar(StatusAluno status) {
        return alunos.listar(status);
    }

    public FichaAluno ficha(UUID id) {
        Aluno aluno = buscar(id);
        Responsavel responsavel = responsaveis.buscarPorId(aluno.responsavelId()).orElse(null);
        return new FichaAluno(aluno, responsavel);
    }

    public Aluno atualizar(UUID id, AtualizarAlunoCommand comando, Ator ator) {
        Aluno aluno = buscar(id);
        aluno.atualizarFicha(comando.nome(), comando.dataNascimento(), comando.informacoesMedicas(),
                comando.hospitalEmergencia(), comando.autorizados(), comando.fotoUrl());
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Aluno", id, "Ficha do aluno atualizada");
        return alunos.salvar(aluno);
    }

    /** Inscrição em Jiu Jitsu / Ballet (cobrados à parte a partir da próxima geração mensal). */
    public Aluno definirAtividades(UUID id, Set<AtividadeExtracurricular> atividades, Ator ator) {
        Aluno aluno = buscar(id);
        aluno.definirAtividades(atividades);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Aluno", id, "Atividades extracurriculares: " + atividades);
        return alunos.salvar(aluno);
    }

    /** Correção dos dados do responsável financeiro (nome, contato, parentesco). O CPF não muda. */
    public Responsavel atualizarResponsavel(UUID alunoId, String nome, Contato contato, String parentesco,
            Ator ator) {
        Responsavel responsavel = responsavel(buscar(alunoId));
        responsavel.atualizar(nome, contato, parentesco);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Responsavel", responsavel.id(), "Dados do responsável");
        return responsaveis.salvar(responsavel);
    }

    /** Link para o responsável criar (ou redefinir) a senha do portal. */
    public Convite acessoDoResponsavel(UUID alunoId, Ator ator) {
        Responsavel responsavel = responsavel(buscar(alunoId));
        Convite convite = convites.convidar(responsavel.nome(), responsavel.contato().email(), Perfil.RESPONSAVEL,
                responsavel.cpf());
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Usuario", convite.usuario().id(),
                "Link de acesso do responsável " + convite.usuario().email());
        return convite;
    }

    public Aluno inativar(UUID id, Ator ator) {
        Aluno aluno = buscar(id);
        aluno.inativar();
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Aluno", id, aluno.nome());
        return alunos.salvar(aluno);
    }

    private Responsavel responsavel(Aluno aluno) {
        return responsaveis.buscarPorId(aluno.responsavelId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Responsável", aluno.responsavelId()));
    }

    private Aluno buscar(UUID id) {
        return alunos.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
    }

    public record AtualizarAlunoCommand(String nome, LocalDate dataNascimento, InformacoesMedicas informacoesMedicas,
            String hospitalEmergencia, List<PessoaAutorizada> autorizados, String fotoUrl) {
    }

    public record FichaAluno(Aluno aluno, Responsavel responsavel) {
    }
}
