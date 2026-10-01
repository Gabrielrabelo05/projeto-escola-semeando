package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.shared.Validacoes;

/**
 * Tabela oficial de valores vigente.
 * RN-02: mensalidade do Ensino Fundamental fixa em R$ 375,00 x 11 parcelas (total R$ 4.125,00).
 */
public record TabelaValores(Dinheiro mensalidadeFundamental, int parcelasFundamental, Dinheiro taxaAssociadoInfantil,
        int parcelasAssociado, Dinheiro taxaMatricula, int diaVencimento) {

    public static final Dinheiro MENSALIDADE_OFICIAL = Dinheiro.de("375.00");
    public static final int PARCELAS_OFICIAIS = 11;

    public TabelaValores {
        Validacoes.obrigatorio(mensalidadeFundamental, "mensalidade do fundamental");
        Validacoes.obrigatorio(taxaAssociadoInfantil, "taxa de associado");
        Validacoes.obrigatorio(taxaMatricula, "taxa de matrícula");
        Validacoes.positivo(parcelasAssociado, "parcelas da taxa de associado");
        if (diaVencimento < 1 || diaVencimento > 28) {
            throw new RegraNegocioVioladaException("CONFIG", "Dia de vencimento deve estar entre 1 e 28");
        }
        if (!mensalidadeFundamental.equals(MENSALIDADE_OFICIAL) || parcelasFundamental != PARCELAS_OFICIAIS) {
            throw new RegraNegocioVioladaException("RN-02",
                    "Tabela de valores diverge da oficial: R$ 375,00 em 11 parcelas");
        }
    }

    public Dinheiro totalAnualFundamental() {
        return mensalidadeFundamental.multiplicar(parcelasFundamental);
    }

    /** RF-17 / RN-02: valores do contrato precisam ser idênticos aos da tabela oficial. */
    public void validarValoresContrato(Dinheiro valorParcela, int numeroParcelas) {
        if (!mensalidadeFundamental.equals(valorParcela) || parcelasFundamental != numeroParcelas) {
            throw new RegraNegocioVioladaException("RN-02", "Valores divergentes da tabela oficial (" + valorParcela
                    + " x " + numeroParcelas + "). Esperado " + mensalidadeFundamental + " x " + parcelasFundamental
                    + ". A geração do contrato foi bloqueada.");
        }
    }

    public Dinheiro valorMensalPara(TipoVinculo tipo) {
        return tipo == TipoVinculo.FICHA_SOCIO ? taxaAssociadoInfantil : mensalidadeFundamental;
    }

    public int parcelasPara(TipoVinculo tipo) {
        return tipo == TipoVinculo.FICHA_SOCIO ? parcelasAssociado : parcelasFundamental;
    }
}
