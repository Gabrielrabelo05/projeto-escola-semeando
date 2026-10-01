package br.org.semeando.digital.domain.obrigacao;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

/**
 * RN-10: prazos regulatórios não negociáveis.
 * SMTT 31/03, Censo Escolar 30/06 e Sistema Presença no último dia de cada bimestre.
 */
public enum TipoObrigacao {
    SMTT("Renovação do alvará de transporte escolar (SMTT)", List.of(
            "Alvará anterior", "CRLV do veículo", "CNH categoria D do condutor",
            "Certidão negativa criminal do condutor", "Laudo de vistoria do veículo", "Apólice de seguro")),
    CENSO_ESCOLAR("Censo Escolar (Educacenso)", List.of(
            "Dados cadastrais da escola", "Dados de turmas", "Dados de alunos", "Dados de profissionais")),
    SISTEMA_PRESENCA("Sistema Presença (frequência bimestral)", List.of(
            "Frequência bimestral exportada", "Justificativas de baixa frequência")),
    OUTRA("Outra obrigação", List.of());

    private final String descricao;
    private final List<String> checklistPadrao;

    TipoObrigacao(String descricao, List<String> checklistPadrao) {
        this.descricao = descricao;
        this.checklistPadrao = checklistPadrao;
    }

    /**
     * Calcula o prazo legal. Para o Sistema Presença o ano é dividido em 4 bimestres letivos
     * (fev-mar, abr-mai, ago-set, out-nov); o prazo é o último dia do bimestre.
     */
    public LocalDate prazo(int ano, Integer bimestre) {
        return switch (this) {
            case SMTT -> LocalDate.of(ano, Month.MARCH, 31);
            case CENSO_ESCOLAR -> LocalDate.of(ano, Month.JUNE, 30);
            case SISTEMA_PRESENCA -> ultimoDiaDoBimestre(ano, bimestre);
            case OUTRA -> throw new IllegalStateException("Obrigações do tipo OUTRA têm prazo informado manualmente");
        };
    }

    public boolean prazoFixo() {
        return this != OUTRA;
    }

    private static LocalDate ultimoDiaDoBimestre(int ano, Integer bimestre) {
        if (bimestre == null || bimestre < 1 || bimestre > 4) {
            throw new IllegalArgumentException("Bimestre deve estar entre 1 e 4");
        }
        Month[] fimBimestre = {Month.MARCH, Month.MAY, Month.SEPTEMBER, Month.NOVEMBER};
        return YearMonth.of(ano, fimBimestre[bimestre - 1]).atEndOfMonth();
    }

    public String descricao() {
        return descricao;
    }

    public List<String> checklistPadrao() {
        return checklistPadrao;
    }
}
