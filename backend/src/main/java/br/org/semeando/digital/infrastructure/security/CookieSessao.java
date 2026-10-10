package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * O JWT viaja só em cookie HttpOnly (o JavaScript da página não consegue ler, então um XSS não rouba a sessão),
 * Secure (apenas HTTPS) e SameSite=Strict (não é enviado por outros sites, o que barra CSRF). O frontend chama a
 * API pelo mesmo domínio (proxy /api do Next), então o cookie é de primeira parte.
 */
@Component
public class CookieSessao {

    public static final String NOME = "semeando_sessao";
    static final String CAMINHO = "/api";

    private final boolean seguro;
    private final Duration validade;

    CookieSessao(SemeandoProperties props) {
        this.seguro = props.seguranca().cookieSeguro();
        this.validade = Duration.ofMinutes(props.seguranca().jwtValidadeMinutos());
    }

    public ResponseCookie criar(String token) {
        return base(token).maxAge(validade).build();
    }

    public ResponseCookie apagar() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOME, valor).httpOnly(true).secure(seguro).sameSite("Strict").path(CAMINHO);
    }
}
