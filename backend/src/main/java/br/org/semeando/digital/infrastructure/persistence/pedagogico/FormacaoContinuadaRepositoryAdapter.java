package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.FormacaoContinuada;
import br.org.semeando.digital.domain.pedagogico.FormacaoContinuadaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class FormacaoContinuadaRepositoryAdapter implements FormacaoContinuadaRepository {

    private final FormacaoContinuadaJpaRepository jpa;

    FormacaoContinuadaRepositoryAdapter(FormacaoContinuadaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public FormacaoContinuada salvar(FormacaoContinuada formacao) {
        return jpa.save(FormacaoContinuadaEntity.de(formacao)).paraDominio();
    }

    @Override
    public List<FormacaoContinuada> listar(UUID professorId) {
        var lista = professorId == null ? jpa.findAllByOrderByDataDesc()
                : jpa.findByProfessorIdOrderByDataDesc(professorId);
        return lista.stream().map(FormacaoContinuadaEntity::paraDominio).toList();
    }
}
