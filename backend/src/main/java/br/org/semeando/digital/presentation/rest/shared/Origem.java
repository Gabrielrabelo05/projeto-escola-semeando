package br.org.semeando.digital.presentation.rest.shared;

import jakarta.servlet.http.HttpServletRequest;

/** IP de origem da requisição (a API fica atrás de proxies: Vercel e Render). */
public final class Origem {

    private Origem() {
    }

    public static String ip(HttpServletRequest http) {
        String encaminhado = http.getHeader("X-Forwarded-For");
        return encaminhado == null || encaminhado.isBlank() ? http.getRemoteAddr()
                : encaminhado.split(",")[0].trim();
    }
}
