package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.application.port.EmissorToken;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

/**
 * Sessão e desafio usam emissores diferentes: o decodificador da sessão exige {@link #EMISSOR}, então um token de
 * desafio (que só prova a senha) nunca é aceito como sessão.
 */
@Component
class JwtEmissorToken implements EmissorToken {

    static final String CLAIM_PERFIL = "perfil";
    static final String CLAIM_NOME = "nome";
    static final String CLAIM_CPF = "cpf";
    /** {@code true} quando o login conferiu o código do aplicativo autenticador. */
    static final String CLAIM_SEGUNDO_FATOR = "mfa";
    static final String EMISSOR = "semeando-digital";
    static final String EMISSOR_DESAFIO = "semeando-digital/desafio-2fa";

    private final JwtEncoder encoder;
    private final JwtDecoder decoderDesafio;
    private final Duration validade;
    private final Clock clock;

    JwtEmissorToken(JwtEncoder encoder, SemeandoProperties props, Clock clock) {
        this.encoder = encoder;
        this.validade = Duration.ofMinutes(props.seguranca().jwtValidadeMinutos());
        this.clock = clock;
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(SegurancaConfig.chave(props))
                .macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISSOR_DESAFIO));
        this.decoderDesafio = decoder;
    }

    @Override
    public String emitir(Usuario usuario, boolean comSegundoFator) {
        Instant agora = clock.instant();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.id().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim(CLAIM_NOME, usuario.nome())
                .claim(CLAIM_PERFIL, usuario.perfil().name())
                .claim(CLAIM_SEGUNDO_FATOR, comSegundoFator);
        if (usuario.cpf() != null) {
            claims.claim(CLAIM_CPF, usuario.cpf().numero());
        }
        return assinar(claims.build());
    }

    @Override
    public String emitirDesafio(Usuario usuario) {
        Instant agora = clock.instant();
        return assinar(JwtClaimsSet.builder()
                .issuer(EMISSOR_DESAFIO)
                .subject(usuario.id().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE_DESAFIO))
                .build());
    }

    @Override
    public Optional<Desafio> lerDesafio(String token) {
        try {
            Jwt jwt = decoderDesafio.decode(token);
            return Optional.of(new Desafio(UUID.fromString(jwt.getSubject()), jwt.getIssuedAt()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String assinar(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
