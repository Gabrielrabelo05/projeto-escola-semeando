package br.org.semeando.digital.domain.pedagogico;

import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-40: registro de conteúdo ministrado e presença/falta de cada aluno por aula. */
public class DiarioClasse {

    private final UUID id;
    private final UUID professorId;
    private final UUID turmaId;
    private final LocalDate data;
    private String conteudoMinistrado;
    private List<Presenca> presencas;

    public DiarioClasse(UUID id, UUID professorId, UUID turmaId, LocalDate data, String conteudoMinistrado,
            List<Presenca> presencas) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.professorId = Validacoes.obrigatorio(professorId, "professor");
        this.turmaId = Validacoes.obrigatorio(turmaId, "turma");
        this.data = Validacoes.obrigatorio(data, "data");
        registrar(conteudoMinistrado, presencas);
    }

    public static DiarioClasse registrar(UUID professorId, UUID turmaId, LocalDate data, String conteudo,
            List<Presenca> presencas) {
        return new DiarioClasse(UUID.randomUUID(), professorId, turmaId, data, conteudo, presencas);
    }

    public final void registrar(String conteudoMinistrado, List<Presenca> presencas) {
        this.conteudoMinistrado = Validacoes.obrigatorio(conteudoMinistrado, "conteúdo ministrado");
        this.presencas = List.copyOf(Validacoes.naoVazio(presencas, "presenças"));
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

    public String conteudoMinistrado() {
        return conteudoMinistrado;
    }

    public List<Presenca> presencas() {
        return presencas;
    }
}
