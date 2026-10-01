package br.org.semeando.digital.infrastructure.persistence.notificacao;

import br.org.semeando.digital.domain.usuario.Perfil;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface NotificacaoInternaJpaRepository extends JpaRepository<NotificacaoInternaEntity, UUID> {

    List<NotificacaoInternaEntity> findTop100ByPerfilDestinoInOrderByCriadaEmDesc(Collection<Perfil> perfis);

    List<NotificacaoInternaEntity> findTop100ByPerfilDestinoInAndLidaFalseOrderByCriadaEmDesc(
            Collection<Perfil> perfis);
}
