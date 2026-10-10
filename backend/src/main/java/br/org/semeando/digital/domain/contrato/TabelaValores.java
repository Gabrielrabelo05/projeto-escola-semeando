package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.shared.Validacoes;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import br.org.semeando.digital.domain.turma.Serie;

/**
 * Tabela oficial de valores vigente (Anexo didático do Centro Educacional Semeando, 2026).
 * RN-02: a mensalidade do Ensino Fundamental é fixa por série, em 11 parcelas:
 * 1º ano R$ 310,00 (R$ 279,00 até o vencimento) e 2º ao 5º ano R$ 375,00 (R$ 337,00 até o vencimento).
 * A Educação Infantil paga taxa de associado (R$ 125,00, sem boleto) e Jiu Jitsu / Ballet custam R$ 50,00.
 */
public record TabelaValores(Mensalidade primeiroAno, Mensalidade segundoAoQuintoAno, int parcelasFundamental,
        Dinheiro taxaAssociadoInfantil, int parcelasAssociado, Dinheiro atividadeExtracurricular, int diaVencimento) {

    public static final Mensalidade PRIMEIRO_ANO_OFICIAL = Mensalidade.de("310.00", "279.00");
    public static final Mensalidade SEGUNDO_AO_QUINTO_ANO_OFICIAL = Mensalidade.de("375.00", "337.00");
    public static final int PARCELAS_OFICIAIS = 11;

    public TabelaValores {
        Validacoes.obrigatorio(primeiroAno, "mensalidade do 1º ano");
        Validacoes.obrigatorio(segundoAoQuintoAno, "mensalidade do 2º ao 5º ano");
        Validacoes.obrigatorio(taxaAssociadoInfantil, "taxa de associado");
        Validacoes.obrigatorio(atividadeExtracurricular, "valor da atividade extracurricular");
        Validacoes.positivo(parcelasAssociado, "parcelas da taxa de associado");
        if (diaVencimento < 1 || diaVencimento > 28) {
            throw new RegraNegocioVioladaException("CONFIG", "Dia de vencimento deve estar entre 1 e 28");
        }
        if (!primeiroAno.equals(PRIMEIRO_ANO_OFICIAL) || !segundoAoQuintoAno.equals(SEGUNDO_AO_QUINTO_ANO_OFICIAL)
                || parcelasFundamental != PARCELAS_OFICIAIS) {
            throw new RegraNegocioVioladaException("RN-02", "Tabela de valores diverge da oficial: 1º ano R$ 310,00 "
                    + "(R$ 279,00 com desconto) e 2º ao 5º ano R$ 375,00 (R$ 337,00 com desconto), em 11 parcelas");
        }
    }

    /** Mensalidade do Ensino Fundamental para a série. */
    public Mensalidade mensalidade(Serie serie) {
        if (serie.nivel() != NivelEnsino.ENSINO_FUNDAMENTAL) {
            throw new DomainException("RN-01", serie.descricao() + " não possui mensalidade: paga taxa de associado");
        }
        return serie == Serie.PRIMEIRO_ANO ? primeiroAno : segundoAoQuintoAno;
    }

    /** Valor da cobrança mensal principal: taxa de associado (Infantil) ou mensalidade (Fundamental). */
    public Mensalidade valorMensalPara(Serie serie) {
        return serie.nivel() == NivelEnsino.EDUCACAO_INFANTIL ? Mensalidade.semDesconto(taxaAssociadoInfantil)
                : mensalidade(serie);
    }

    public Mensalidade extracurriculares(int quantidade) {
        return Mensalidade.semDesconto(atividadeExtracurricular.multiplicar(quantidade));
    }

    public Dinheiro totalAnual(Serie serie) {
        return mensalidade(serie).integral().multiplicar(parcelasFundamental);
    }

    /** RF-17 / RN-02: valores do contrato precisam ser idênticos aos da tabela oficial da série. */
    public void validarValoresContrato(Serie serie, Dinheiro valorParcela, int numeroParcelas) {
        Dinheiro esperado = mensalidade(serie).integral();
        if (!esperado.equals(valorParcela) || parcelasFundamental != numeroParcelas) {
            throw new RegraNegocioVioladaException("RN-02", "Valores divergentes da tabela oficial (" + valorParcela
                    + " x " + numeroParcelas + "). Esperado para o " + serie.descricao() + ": " + esperado + " x "
                    + parcelasFundamental + ". A geração do contrato foi bloqueada.");
        }
    }
}
