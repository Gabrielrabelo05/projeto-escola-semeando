package br.org.semeando.digital.domain.pedagogico;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.UUID;

/** RF-39 / RF-46: plano de aula criado pelo professor e aprovado pela coordenação. */
public class PlanoAula {

    private final UUID id;
    private final UUID professorId;
    private final UUID turmaId;
    private LocalDate data;
    private ConteudoPlano conteudo;
    private StatusPlanoAula status;
    private String comentarioCoordenacao;

    public PlanoAula(UUID id, UUID professorId, UUID turmaId, LocalDate data, ConteudoPlano conteudo,
            StatusPlanoAula status, String comentarioCoordenacao) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.professorId = Validacoes.obrigatorio(professorId, "professor");
        this.turmaId = Validacoes.obrigatorio(turmaId, "turma");
        this.data = Validacoes.obrigatorio(data, "data");
        this.conteudo = Validacoes.obrigatorio(conteudo, "conteúdo do plano");
        this.status = Validacoes.obrigatorio(status, "status");
        this.comentarioCoordenacao = comentarioCoordenacao;
    }

    public static PlanoAula criar(UUID professorId, UUID turmaId, LocalDate data, ConteudoPlano conteudo) {
        return new PlanoAula(UUID.randomUUID(), professorId, turmaId, data, conteudo, StatusPlanoAula.RASCUNHO,
                null);
    }

    public void editar(LocalDate data, ConteudoPlano conteudo) {
        if (status == StatusPlanoAula.SUBMETIDO || status == StatusPlanoAula.APROVADO) {
            throw new DomainException("STATUS_INVALIDO", "Plano " + status + " não pode ser editado");
        }
        this.data = Validacoes.obrigatorio(data, "data");
        this.conteudo = Validacoes.obrigatorio(conteudo, "conteúdo do plano");
    }

    public void submeter() {
        if (status != StatusPlanoAula.RASCUNHO && status != StatusPlanoAula.DEVOLVIDO) {
            throw new DomainException("STATUS_INVALIDO", "Somente rascunhos ou planos devolvidos podem ser submetidos");
        }
        this.status = StatusPlanoAula.SUBMETIDO;
    }

    public void aprovar(String comentario) {
        exigirSubmetido();
        this.comentarioCoordenacao = comentario;
        this.status = StatusPlanoAula.APROVADO;
    }

    public void devolver(String comentario) {
        exigirSubmetido();
        this.comentarioCoordenacao = Validacoes.obrigatorio(comentario, "comentário da coordenação");
        this.status = StatusPlanoAula.DEVOLVIDO;
    }

    private void exigirSubmetido() {
        if (status != StatusPlanoAula.SUBMETIDO) {
            throw new DomainException("STATUS_INVALIDO", "O plano precisa estar submetido para análise");
        }
    }

    public boolean pertenceA(UUID professorId) {
        return this.professorId.equals(professorId);
    }

    public UUID id() {
        return id;
    }

    public UUID professorId() {
        return professorId;
    }

    public UUID turmaId() {
        return turmaId;
    }

    public LocalDate data() {
        return data;
    }

    public ConteudoPlano conteudo() {
        return conteudo;
    }

    public StatusPlanoAula status() {
        return status;
    }

    public String comentarioCoordenacao() {
        return comentarioCoordenacao;
    }
}
