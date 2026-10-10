package br.org.semeando.digital.domain.aluno;

/** Atividades extracurriculares oferecidas pela escola, cobradas à parte (tabela oficial: R$ 50,00 cada). */
public enum AtividadeExtracurricular {
    JIU_JITSU("Jiu Jitsu"),
    BALLET("Ballet");

    private final String descricao;

    AtividadeExtracurricular(String descricao) {
        this.descricao = descricao;
    }

    public String descricao() {
        return descricao;
    }
}
