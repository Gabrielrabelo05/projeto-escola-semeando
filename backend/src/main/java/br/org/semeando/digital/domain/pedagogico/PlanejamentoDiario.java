package br.org.semeando.digital.domain.pedagogico;

import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.UUID;

/** RF-41: roteiro diário do professor, que a coordenação pode comentar. */
public class PlanejamentoDiario {

    private final UUID id;
    private final UUID professorId;
    private final UUID turmaId;
    private final LocalDate data;
    private String roteiro;
    private String comentarioCoordenacao;

    public PlanejamentoDiario(UUID id, UUID professorId, UUID turmaId, LocalDate data, String roteiro,
            String comentarioCoordenacao) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.professorId = Validacoes.obrigatorio(professorId, "professor");
        this.turmaId = Validacoes.obrigatorio(turmaId, "turma");
        this.data = Validacoes.obrigatorio(data, "data");
        this.roteiro = Validacoes.obrigatorio(roteiro, "roteiro");
        this.comentarioCoordenacao = comentarioCoordenacao;
    }

    public static PlanejamentoDiario criar(UUID professorId, UUID turmaId, LocalDate data, String roteiro) {
        return new PlanejamentoDiario(UUID.randomUUID(), professorId, turmaId, data, roteiro, null);
    }

    public void comentar(String comentario) {
        this.comentarioCoordenacao = Validacoes.obrigatorio(comentario, "comentário");
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

    public String roteiro() {
        return roteiro;
    }

    public String comentarioCoordenacao() {
        return comentarioCoordenacao;
    }
}
