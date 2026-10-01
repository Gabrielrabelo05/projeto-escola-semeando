package br.org.semeando.digital.domain.turma;

import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.util.UUID;

public class Turma {

    private final UUID id;
    private String nome;
    private final Serie serie;
    private final int anoLetivo;
    private int capacidadeMaxima;
    private UUID professorTitularId;

    public Turma(UUID id, String nome, Serie serie, int anoLetivo, int capacidadeMaxima, UUID professorTitularId) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.nome = Validacoes.obrigatorio(nome, "nome da turma");
        this.serie = Validacoes.obrigatorio(serie, "série");
        this.anoLetivo = Validacoes.positivo(anoLetivo, "ano letivo");
        this.capacidadeMaxima = Validacoes.positivo(capacidadeMaxima, "capacidade máxima");
        this.professorTitularId = professorTitularId;
    }

    public static Turma nova(String nome, Serie serie, int anoLetivo, int capacidadeMaxima, UUID professorTitularId) {
        return new Turma(UUID.randomUUID(), nome, serie, anoLetivo, capacidadeMaxima, professorTitularId);
    }

    /** RF-09: bloqueia novas matrículas quando a turma atinge a capacidade máxima. */
    public void garantirVaga(long matriculasAtivas) {
        if (vagasDisponiveis(matriculasAtivas) <= 0) {
            throw new RegraNegocioVioladaException("RF-09", "Turma " + nome + " atingiu a capacidade máxima");
        }
    }

    public long vagasDisponiveis(long matriculasAtivas) {
        return Math.max(0, capacidadeMaxima - matriculasAtivas);
    }

    public void atualizar(String nome, int capacidadeMaxima, UUID professorTitularId) {
        this.nome = Validacoes.obrigatorio(nome, "nome da turma");
        this.capacidadeMaxima = Validacoes.positivo(capacidadeMaxima, "capacidade máxima");
        this.professorTitularId = professorTitularId;
    }

    public NivelEnsino nivel() {
        return serie.nivel();
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public Serie serie() {
        return serie;
    }

    public int anoLetivo() {
        return anoLetivo;
    }

    public int capacidadeMaxima() {
        return capacidadeMaxima;
    }

    public UUID professorTitularId() {
        return professorTitularId;
    }
}
