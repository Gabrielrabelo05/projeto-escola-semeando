package br.org.semeando.digital.domain.usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConviteAcessoRepository {

    ConviteAcesso salvar(ConviteAcesso convite);

    Optional<ConviteAcesso> buscarPorTokenHash(String tokenHash);

    List<ConviteAcesso> listarPorUsuario(UUID usuarioId);
}
