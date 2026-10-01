package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.UUID;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Converte o JWT autenticado no {@link Ator} recebido pelos controllers e casos de uso. */
@Component
public class AtorArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Ator.class.equals(parameter.getParameterType());
    }

    @Override
    public Ator resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (!(autenticacao instanceof JwtAuthenticationToken token)) {
            return Ator.anonimo();
        }
        var jwt = token.getToken();
        String cpf = jwt.getClaimAsString(JwtEmissorToken.CLAIM_CPF);
        return new Ator(UUID.fromString(jwt.getSubject()), jwt.getClaimAsString(JwtEmissorToken.CLAIM_NOME),
                Perfil.valueOf(jwt.getClaimAsString(JwtEmissorToken.CLAIM_PERFIL)), cpf == null ? null : Cpf.de(cpf));
    }
}
