package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * RF-35: autenticação stateless via JWT e autorização por perfil (RBAC) com @PreAuthorize. O token vem do cookie
 * HttpOnly de sessão ({@link CookieSessao}) e cada requisição confere se a sessão continua ativa.
 */
@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    private static final String[] ROTAS_PUBLICAS = {"/api/auth/login", "/api/auth/sair", "/api/pre-matriculas",
        "/api/publico/**", "/api/webhooks/cora", "/actuator/health"};

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, CookieSessao cookie) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/sair").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pre-matriculas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/publico/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/publico/convites/*/senha").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/webhooks/cora").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .bearerTokenResolver(new ResolvedorTokenSessao(ROTAS_PUBLICAS))
                        .authenticationEntryPoint(semSessao(cookie))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorPerfis())))
                .build();
    }

    /** 401 que também apaga o cookie vencido ou revogado, para o navegador não continuar enviando. */
    private static AuthenticationEntryPoint semSessao(CookieSessao cookie) {
        BearerTokenAuthenticationEntryPoint padrao = new BearerTokenAuthenticationEntryPoint();
        return (request, response, erro) -> {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.apagar().toString());
            padrao.commence(request, response, erro);
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Custo 12: cada tentativa de adivinhar a senha fica ~4x mais cara que o padrão (10).
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    JwtEncoder jwtEncoder(SemeandoProperties props) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave(props)));
    }

    @Bean
    JwtDecoder jwtDecoder(SemeandoProperties props, UsuarioRepository usuarios) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chave(props)).macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(JwtEmissorToken.EMISSOR), new ValidadorSessaoAtiva(usuarios)));
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(SemeandoProperties props) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(props.seguranca().corsOrigens());
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", ResolvedorTokenSessao.HEADER_CSRF));
        cors.setAllowCredentials(true);
        cors.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    private static JwtAuthenticationConverter conversorPerfis() {
        JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
        perfis.setAuthoritiesClaimName(JwtEmissorToken.CLAIM_PERFIL);
        perfis.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(perfis);
        return conversor;
    }

    private static SecretKey chave(SemeandoProperties props) {
        byte[] bytes = props.seguranca().jwtSegredo().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("O segredo JWT deve ter ao menos 32 bytes");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
