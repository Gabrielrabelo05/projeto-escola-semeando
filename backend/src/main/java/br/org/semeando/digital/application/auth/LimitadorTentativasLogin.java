package br.org.semeando.digital.application.auth;

import br.org.semeando.digital.domain.shared.DomainException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proteção contra força bruta no login: 5 erros seguidos do mesmo e-mail e origem, ou 20 do mesmo e-mail em
 * qualquer origem, bloqueiam novas tentativas por 15 minutos. Fica em memória (a API roda em uma instância).
 */
public class LimitadorTentativasLogin {

    static final int LIMITE_POR_ORIGEM = 5;
    static final int LIMITE_POR_EMAIL = 20;
    static final Duration JANELA = Duration.ofMinutes(15);

    private final Map<String, Janela> falhas = new ConcurrentHashMap<>();
    private final Clock clock;

    public LimitadorTentativasLogin(Clock clock) {
        this.clock = clock;
    }

    public void verificar(String email, String origem) {
        Instant agora = clock.instant();
        if (bloqueado(chave(email, origem), LIMITE_POR_ORIGEM, agora) || bloqueado(email, LIMITE_POR_EMAIL, agora)) {
            throw new DomainException("MUITAS_TENTATIVAS",
                    "Muitas tentativas de login sem sucesso. Aguarde 15 minutos e tente novamente.");
        }
    }

    public void registrarFalha(String email, String origem) {
        Instant agora = clock.instant();
        incrementar(chave(email, origem), agora);
        incrementar(email, agora);
        if (falhas.size() > 10_000) {
            falhas.values().removeIf(j -> j.expirou(agora));
        }
    }

    public void registrarSucesso(String email, String origem) {
        falhas.remove(chave(email, origem));
    }

    private boolean bloqueado(String chave, int limite, Instant agora) {
        Janela janela = falhas.get(chave);
        return janela != null && !janela.expirou(agora) && janela.quantidade() >= limite;
    }

    private void incrementar(String chave, Instant agora) {
        falhas.compute(chave, (k, atual) -> atual == null || atual.expirou(agora)
                ? new Janela(1, agora) : new Janela(atual.quantidade() + 1, atual.inicio()));
    }

    private static String chave(String email, String origem) {
        return email + "|" + (origem == null ? "" : origem);
    }

    private record Janela(int quantidade, Instant inicio) {

        boolean expirou(Instant agora) {
            return !agora.isBefore(inicio.plus(JANELA));
        }
    }
}
