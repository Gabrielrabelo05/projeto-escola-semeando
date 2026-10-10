package br.org.semeando.digital.infrastructure.persistence.usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ConviteAcessoJpaRepository extends JpaRepository<ConviteAcessoEntity, UUID> {

    Optional<ConviteAcessoEntity> findByTokenHash(String tokenHash);

    List<ConviteAcessoEntity> findByUsuarioId(UUID usuarioId);
}
