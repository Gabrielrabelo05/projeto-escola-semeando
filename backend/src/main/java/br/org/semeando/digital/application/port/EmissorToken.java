package br.org.semeando.digital.application.port;

import br.org.semeando.digital.domain.usuario.Usuario;

public interface EmissorToken {

    String emitir(Usuario usuario);
}
