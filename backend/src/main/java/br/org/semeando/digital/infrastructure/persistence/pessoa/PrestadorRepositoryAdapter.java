package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.PrestadorServico;
import br.org.semeando.digital.domain.pessoa.PrestadorServicoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class PrestadorRepositoryAdapter implements PrestadorServicoRepository {

    private final PrestadorJpaRepository jpa;

    PrestadorRepositoryAdapter(PrestadorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PrestadorServico salvar(PrestadorServico prestador) {
        return jpa.save(PrestadorEntity.de(prestador)).paraDominio();
    }

    @Override
    public Optional<PrestadorServico> buscarPorId(UUID id) {
        return jpa.findById(id).map(PrestadorEntity::paraDominio);
    }

    @Override
    public List<PrestadorServico> listar(boolean incluirInativos) {
        var lista = incluirInativos ? jpa.findAllByOrderByNome() : jpa.findByAtivoTrueOrderByNome();
        return lista.stream().map(PrestadorEntity::paraDominio).toList();
    }
}
