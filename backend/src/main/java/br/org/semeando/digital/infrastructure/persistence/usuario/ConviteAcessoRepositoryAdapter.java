package br.org.semeando.digital.infrastructure.persistence.usuario;

import br.org.semeando.digital.domain.usuario.ConviteAcesso;
import br.org.semeando.digital.domain.usuario.ConviteAcessoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ConviteAcessoRepositoryAdapter implements ConviteAcessoRepository {

    private final ConviteAcessoJpaRepository jpa;

    ConviteAcessoRepositoryAdapter(ConviteAcessoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ConviteAcesso salvar(ConviteAcesso convite) {
        return jpa.save(ConviteAcessoEntity.de(convite)).paraDominio();
    }

    @Override
    public Optional<ConviteAcesso> buscarPorTokenHash(String tokenHash) {
        return jpa.findByTokenHash(tokenHash).map(ConviteAcessoEntity::paraDominio);
    }

    @Override
    public List<ConviteAcesso> listarPorUsuario(UUID usuarioId) {
        return jpa.findByUsuarioId(usuarioId).stream().map(ConviteAcessoEntity::paraDominio).toList();
    }
}
