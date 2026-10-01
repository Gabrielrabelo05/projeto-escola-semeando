package br.org.semeando.digital.domain.material;

import br.org.semeando.digital.domain.turma.Serie;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListaMateriaisRepository {

    ListaMateriais salvar(ListaMateriais lista);

    Optional<ListaMateriais> buscarPorId(UUID id);

    Optional<ListaMateriais> buscarPublicada(Serie serie);

    List<ListaMateriais> listar(Serie serie);
}
