package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.application.port.EmissorToken;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.infrastructure.config.SemeandoProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
class JwtEmissorToken implements EmissorToken {

    static final String CLAIM_PERFIL = "perfil";
    static final String CLAIM_NOME = "nome";
    static final String CLAIM_CPF = "cpf";
    static final String EMISSOR = "semeando-digital";

    private final JwtEncoder encoder;
    private final Duration validade;
    private final Clock clock;

    JwtEmissorToken(JwtEncoder encoder, SemeandoProperties props, Clock clock) {
        this.encoder = encoder;
        this.validade = Duration.ofMinutes(props.seguranca().jwtValidadeMinutos());
        this.clock = clock;
    }

    @Override
    public String emitir(Usuario usuario) {
        Instant agora = clock.instant();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.id().toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim(CLAIM_NOME, usuario.nome())
                .claim(CLAIM_PERFIL, usuario.perfil().name());
        if (usuario.cpf() != null) {
            claims.claim(CLAIM_CPF, usuario.cpf().numero());
        }
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
