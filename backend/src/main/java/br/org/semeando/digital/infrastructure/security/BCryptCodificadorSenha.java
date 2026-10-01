package br.org.semeando.digital.infrastructure.security;

import br.org.semeando.digital.application.port.CodificadorSenha;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptCodificadorSenha implements CodificadorSenha {

    private final PasswordEncoder encoder;

    BCryptCodificadorSenha(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public String codificar(String senha) {
        return encoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return senha != null && encoder.matches(senha, hash);
    }
}
