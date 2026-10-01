package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.Instant;

/** RF-14: ficha de sócio do Instituto para famílias da Educação Infantil. */
public final class FichaSocio extends DocumentoVinculo {

    private final String nomeSocio;
    private final Cpf cpfSocio;
    private final boolean aceiteEstatuto;
    private final Dinheiro valorTaxaAssociado;

    public FichaSocio(DadosVinculo dados, String nomeSocio, Cpf cpfSocio, boolean aceiteEstatuto,
            Dinheiro valorTaxaAssociado) {
        super(dados);
        this.nomeSocio = Validacoes.obrigatorio(nomeSocio, "nome do sócio");
        this.cpfSocio = Validacoes.obrigatorio(cpfSocio, "CPF do sócio");
        if (!aceiteEstatuto) {
            throw new DomainException("ESTATUTO", "O sócio precisa aceitar o estatuto do Instituto");
        }
        this.aceiteEstatuto = true;
        this.valorTaxaAssociado = Validacoes.obrigatorio(valorTaxaAssociado, "taxa de associado");
    }

    public FichaSocio novaVersao(String conteudo, String geradoPor, Instant agora) {
        return new FichaSocio(dadosProximaVersao(conteudo, geradoPor, agora), nomeSocio, cpfSocio, aceiteEstatuto,
                valorTaxaAssociado);
    }

    @Override
    public TipoVinculo tipo() {
        return TipoVinculo.FICHA_SOCIO;
    }

    public String nomeSocio() {
        return nomeSocio;
    }

    public Cpf cpfSocio() {
        return cpfSocio;
    }

    public boolean aceiteEstatuto() {
        return aceiteEstatuto;
    }

    public Dinheiro valorTaxaAssociado() {
        return valorTaxaAssociado;
    }
}
