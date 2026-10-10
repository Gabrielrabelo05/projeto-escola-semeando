package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.domain.usuario.PoliticaDoisFatores;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * A cada requisição confere no banco se o dono do token continua ativo e se a sessão não foi encerrada
 * (inativação ou troca de senha). Assim, inativar um usuário derruba o acesso na hora, sem esperar o token vencer.
 * Para quem tem (ou precisa ter) verificação em duas etapas, só vale a sessão aberta com o código do aplicativo.
 */
class ValidadorSessaoAtiva implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error SESSAO_ENCERRADA = new OAuth2Error("invalid_token",
            "Sessão encerrada: entre novamente", null);

    private final UsuarioRepository usuarios;
    private final PoliticaDoisFatores politica;

    ValidadorSessaoAtiva(UsuarioRepository usuarios, PoliticaDoisFatores politica) {
        this.usuarios = usuarios;
        this.politica = politica;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        Instant emitidoEm = jwt.getIssuedAt();
        boolean comSegundoFator = Boolean.TRUE.equals(jwt.getClaimAsBoolean(JwtEmissorToken.CLAIM_SEGUNDO_FATOR));
        boolean valido = emitidoEm != null && usuarios.buscarPorId(UUID.fromString(jwt.getSubject()))
                .map(u -> u.aceitaSessaoEmitidaEm(emitidoEm) && (comSegundoFator || !politica.exige(u)))
                .orElse(false);
        return valido ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(SESSAO_ENCERRADA);
    }
}
