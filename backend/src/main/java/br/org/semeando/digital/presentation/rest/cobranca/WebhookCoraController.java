package br.org.semeando.digital.presentation.rest.cobranca;

import br.org.semeando.digital.application.cobranca.ConfirmarPagamentoUseCase;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.WebhookCoraRequest;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** RF-28: baixa automática de boleto a partir do webhook do Cora. */
@RestController
@RequestMapping("/api/webhooks/cora")
public class WebhookCoraController {

    private static final String STATUS_PAGO = "PAID";

    private final ConfirmarPagamentoUseCase confirmarPagamento;
    private final byte[] segredo;
    private final Clock clock;

    public WebhookCoraController(ConfirmarPagamentoUseCase confirmarPagamento, SemeandoProperties props,
            Clock clock) {
        this.confirmarPagamento = confirmarPagamento;
        this.segredo = props.cora().webhookSegredo().getBytes(StandardCharsets.UTF_8);
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<Void> receber(@RequestHeader(value = "X-Webhook-Secret", required = false) String assinatura,
            @Valid @RequestBody WebhookCoraRequest evento) {
        if (assinatura == null
                || !MessageDigest.isEqual(segredo, assinatura.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (STATUS_PAGO.equalsIgnoreCase(evento.status()) && evento.totalPaid() != null) {
            LocalDate pagoEm = evento.paidAt() == null ? LocalDate.now(clock) : evento.paidAt();
            confirmarPagamento.processarWebhook(evento.id(), pagoEm, Dinheiro.de(evento.totalPaid()));
        }
        return ResponseEntity.noContent().build();
    }
}
