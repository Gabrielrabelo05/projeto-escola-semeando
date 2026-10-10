package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.application.port.EmissorToken;
import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
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
    /** Desafio da verificação em duas etapas: só vai para as rotas da etapa do código. */
    public static final String NOME_DESAFIO = "semeando_desafio";
    static final String CAMINHO_DESAFIO = "/api/auth/desafio";

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

    public ResponseCookie criarDesafio(String token) {
        return base(NOME_DESAFIO, token, CAMINHO_DESAFIO).maxAge(EmissorToken.VALIDADE_DESAFIO).build();
    }

    public ResponseCookie apagarDesafio() {
        return base(NOME_DESAFIO, "", CAMINHO_DESAFIO).maxAge(Duration.ZERO).build();
    }

    /** Token do desafio, exigindo o header anti-CSRF (as rotas do desafio só recebem POST). */
    public String lerDesafio(HttpServletRequest request) {
        if (request.getHeader(ResolvedorTokenSessao.HEADER_CSRF) == null || request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies()).filter(c -> NOME_DESAFIO.equals(c.getName()))
                .map(Cookie::getValue).filter(v -> !v.isBlank()).findFirst().orElse(null);
    }

    private ResponseCookie.ResponseCookieBuilder base(String valor) {
        return base(NOME, valor, CAMINHO);
    }

    private ResponseCookie.ResponseCookieBuilder base(String nome, String valor, String caminho) {
        return ResponseCookie.from(nome, valor).httpOnly(true).secure(seguro).sameSite("Strict").path(caminho);
    }
}
