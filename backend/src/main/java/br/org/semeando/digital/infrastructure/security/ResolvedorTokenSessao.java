package br.org.semeando.digital.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.AntPathMatcher;

/**
 * Lê o JWT do cookie de sessão (navegador) ou do header Authorization (clientes de API e testes).
 * <ul>
 * <li>Rotas públicas ignoram o cookie: um cookie vencido não pode quebrar o login ou a pré-matrícula.</li>
 * <li>Requisições que alteram dados com o cookie exigem o header {@value #HEADER_CSRF}, que um site de terceiros
 * não consegue enviar (defesa contra CSRF além do SameSite=Strict).</li>
 * </ul>
 */
class ResolvedorTokenSessao implements BearerTokenResolver {

    static final String HEADER_CSRF = "X-Requested-With";
    private static final Set<String> METODOS_SEGUROS = Set.of("GET", "HEAD", "OPTIONS");
    private static final AntPathMatcher CAMINHOS = new AntPathMatcher();

    private final DefaultBearerTokenResolver header = new DefaultBearerTokenResolver();
    private final String[] rotasPublicas;

    ResolvedorTokenSessao(String... rotasPublicas) {
        this.rotasPublicas = rotasPublicas;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        if (publica(request)) {
            return null;
        }
        if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
            return header.resolve(request);
        }
        String token = cookie(request);
        if (token == null) {
            return null;
        }
        boolean alteraDados = !METODOS_SEGUROS.contains(request.getMethod());
        return alteraDados && request.getHeader(HEADER_CSRF) == null ? null : token;
    }

    private boolean publica(HttpServletRequest request) {
        String caminho = request.getRequestURI().substring(request.getContextPath().length());
        return Arrays.stream(rotasPublicas).anyMatch(rota -> CAMINHOS.match(rota, caminho));
    }

    private static String cookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies).filter(c -> CookieSessao.NOME.equals(c.getName()))
                .map(Cookie::getValue).filter(v -> !v.isBlank()).findFirst().orElse(null);
    }
}
