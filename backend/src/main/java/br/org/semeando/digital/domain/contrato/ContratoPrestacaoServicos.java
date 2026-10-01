package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.aluno.PessoaAutorizada;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.Instant;
import java.util.List;

/** RF-13: contrato de prestação de serviços educacionais do Ensino Fundamental. */
public final class ContratoPrestacaoServicos extends DocumentoVinculo {

    private final Dinheiro valorParcela;
    private final int numeroParcelas;
    private final String hospitalEmergencia;
    private final List<PessoaAutorizada> autorizados;

    public ContratoPrestacaoServicos(DadosVinculo dados, Dinheiro valorParcela, int numeroParcelas,
            String hospitalEmergencia, List<PessoaAutorizada> autorizados) {
        super(dados);
        this.valorParcela = Validacoes.obrigatorio(valorParcela, "valor da parcela");
        this.numeroParcelas = Validacoes.positivo(numeroParcelas, "número de parcelas");
        // RN-06: sem hospital de emergência e autorizados o contrato não é gerado.
        if (hospitalEmergencia == null || hospitalEmergencia.isBlank() || autorizados == null
                || autorizados.isEmpty()) {
            throw new RegraNegocioVioladaException("RN-06",
                    "Contrato exige hospital de emergência e lista de autorizados a buscar a criança");
        }
        this.hospitalEmergencia = hospitalEmergencia;
        this.autorizados = List.copyOf(autorizados);
    }

    public ContratoPrestacaoServicos novaVersao(String conteudo, String geradoPor, Instant agora) {
        return new ContratoPrestacaoServicos(dadosProximaVersao(conteudo, geradoPor, agora), valorParcela,
                numeroParcelas, hospitalEmergencia, autorizados);
    }

    @Override
    public TipoVinculo tipo() {
        return TipoVinculo.CONTRATO_PRESTACAO_SERVICOS;
    }

    public Dinheiro valorTotal() {
        return valorParcela.multiplicar(numeroParcelas);
    }

    public Dinheiro valorParcela() {
        return valorParcela;
    }

    public int numeroParcelas() {
        return numeroParcelas;
    }

    public String hospitalEmergencia() {
        return hospitalEmergencia;
    }

    public List<PessoaAutorizada> autorizados() {
        return autorizados;
    }
}
