package br.org.semeando.digital.domain.cobranca;

public enum TipoCobranca {
    /** Ensino Fundamental: boleto Cora, com desconto de pontualidade e encargos do contrato (RN-03). */
    MENSALIDADE("Mensalidade", true),
    /** Educação Infantil: paga na secretaria, sem boleto (ficha de sócio). */
    TAXA_ASSOCIADO("Taxa de associado", false),
    /** Jiu Jitsu / Ballet. */
    EXTRACURRICULAR("Atividade extracurricular", false);

    private final String descricao;
    private final boolean sujeitaAEncargos;

    TipoCobranca(String descricao, boolean sujeitaAEncargos) {
        this.descricao = descricao;
        this.sujeitaAEncargos = sujeitaAEncargos;
    }

    public String descricao() {
        return descricao;
    }

    /** Multa, juros e correção estão previstos no contrato de prestação de serviços, não na ficha de sócio. */
    public boolean sujeitaAEncargos() {
        return sujeitaAEncargos;
    }
}
