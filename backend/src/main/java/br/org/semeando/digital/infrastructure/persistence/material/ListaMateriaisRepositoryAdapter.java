package br.org.semeando.digital.infrastructure.persistence.material;

import br.org.semeando.digital.domain.material.ListaMateriais;
import br.org.semeando.digital.domain.material.ListaMateriaisRepository;
import br.org.semeando.digital.domain.material.StatusLista;
import br.org.semeando.digital.domain.turma.Serie;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ListaMateriaisRepositoryAdapter implements ListaMateriaisRepository {

    private final ListaMateriaisJpaRepository jpa;

    ListaMateriaisRepositoryAdapter(ListaMateriaisJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ListaMateriais salvar(ListaMateriais lista) {
        return jpa.save(ListaMateriaisEntity.de(lista)).paraDominio();
    }

    @Override
    public Optional<ListaMateriais> buscarPorId(UUID id) {
        return jpa.findById(id).map(ListaMateriaisEntity::paraDominio);
    }

    @Override
    public Optional<ListaMateriais> buscarPublicada(Serie serie) {
        return jpa.findFirstBySerieAndStatus(serie, StatusLista.PUBLICADA).map(ListaMateriaisEntity::paraDominio);
    }

    @Override
    public List<ListaMateriais> listar(Serie serie) {
        var lista = serie == null ? jpa.findAllByOrderByAnoLetivoDescSerieAsc()
                : jpa.findBySerieOrderByAnoLetivoDesc(serie);
        return lista.stream().map(ListaMateriaisEntity::paraDominio).toList();
    }
}
