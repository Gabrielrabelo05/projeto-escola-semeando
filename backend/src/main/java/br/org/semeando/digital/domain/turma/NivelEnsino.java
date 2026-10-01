package br.org.semeando.digital.domain.turma;

public enum NivelEnsino {
    EDUCACAO_INFANTIL("Educação Infantil"),
    ENSINO_FUNDAMENTAL("Ensino Fundamental");

    private final String descricao;

    NivelEnsino(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
