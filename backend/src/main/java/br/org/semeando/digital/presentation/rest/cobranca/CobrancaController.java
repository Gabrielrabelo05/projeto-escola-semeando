package br.org.semeando.digital.presentation.rest.cobranca;

import br.org.semeando.digital.application.cobranca.ConfirmarPagamentoUseCase;
import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase;
import br.org.semeando.digital.application.cobranca.ConsultarFinanceiroUseCase.CobrancaComAluno;
import br.org.semeando.digital.application.cobranca.GerarCobrancasMensaisUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.CobrancaResponse;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.GerarMensalRequest;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.InadimplenteResponse;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.PagamentoManualRequest;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.RelatorioResponse;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.ResultadoGeracaoResponse;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Módulo 4 – Cobrança / Cora (RF-23 a RF-30) e RF-51. */
@RestController
@RequestMapping("/api/cobrancas")
public class CobrancaController {

    private final ConsultarFinanceiroUseCase financeiro;
    private final GerarCobrancasMensaisUseCase gerarMensais;
    private final ConfirmarPagamentoUseCase confirmarPagamento;
    private final Clock clock;

    public CobrancaController(ConsultarFinanceiroUseCase financeiro, GerarCobrancasMensaisUseCase gerarMensais,
            ConfirmarPagamentoUseCase confirmarPagamento, Clock clock) {
        this.financeiro = financeiro;
        this.gerarMensais = gerarMensais;
        this.confirmarPagamento = confirmarPagamento;
        this.clock = clock;
    }

    @GetMapping
    @PreAuthorize(Perfis.FINANCEIRO_OU_SECRETARIA)
    public List<CobrancaResponse> listar(@RequestParam String mes) {
        return financeiro.listar(MesReferencia.de(mes)).stream().map(c -> CobrancaResponse.de(c, clock)).toList();
    }

    @GetMapping("/aluno/{alunoId}")
    @PreAuthorize(Perfis.FINANCEIRO_OU_SECRETARIA)
    public List<CobrancaResponse> doAluno(@PathVariable UUID alunoId) {
        return financeiro.doAluno(alunoId).stream().map(c -> CobrancaResponse.de(c, clock)).toList();
    }

    @PostMapping("/gerar-mensal")
    @PreAuthorize(Perfis.FINANCEIRO)
    public ResultadoGeracaoResponse gerarMensal(@Valid @RequestBody GerarMensalRequest request, Ator ator) {
        var resultado = gerarMensais.executar(MesReferencia.de(request.mes()), ator);
        return new ResultadoGeracaoResponse(resultado.mes().toString(), resultado.geradas(), resultado.ignoradas());
    }

    @PostMapping("/{id}/pagamento-manual")
    @PreAuthorize(Perfis.FINANCEIRO_OU_SECRETARIA)
    public CobrancaResponse pagamentoManual(@PathVariable UUID id, @Valid @RequestBody PagamentoManualRequest r,
            Ator ator) {
        var cobranca = confirmarPagamento.registrarManual(id, r.pagoEm(), Dinheiro.de(r.valorPago()), r.forma(), ator);
        return CobrancaResponse.de(new CobrancaComAluno(cobranca, null), clock);
    }

    @GetMapping("/inadimplencia")
    @PreAuthorize(Perfis.FINANCEIRO)
    public List<InadimplenteResponse> inadimplencia(@RequestParam(required = false) String mes) {
        return financeiro.inadimplencia(mes == null ? null : MesReferencia.de(mes)).stream()
                .map(InadimplenteResponse::de).toList();
    }

    @GetMapping("/relatorio")
    @PreAuthorize(Perfis.FINANCEIRO)
    public RelatorioResponse relatorio(@RequestParam String mes) {
        return RelatorioResponse.de(financeiro.relatorioMensal(MesReferencia.de(mes)));
    }

    @GetMapping("/painel")
    @PreAuthorize(Perfis.FINANCEIRO)
    public Map<String, Long> painel(@RequestParam String mes) {
        return financeiro.painelBoletos(MesReferencia.de(mes));
    }
}
