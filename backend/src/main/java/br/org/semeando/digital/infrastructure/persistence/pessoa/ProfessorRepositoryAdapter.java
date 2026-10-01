package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Professor;
import br.org.semeando.digital.domain.pessoa.ProfessorRepository;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.infrastructure.persistence.shared.Termos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ProfessorRepositoryAdapter implements ProfessorRepository {

    private final ProfessorJpaRepository jpa;

    ProfessorRepositoryAdapter(ProfessorJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Professor salvar(Professor entidade) {
        return jpa.save(ProfessorEntity.de(entidade)).paraDominio();
    }

    @Override
    public Optional<Professor> buscarPorId(UUID id) {
        return jpa.findById(id).map(ProfessorEntity::paraDominio);
    }

    @Override
    public Optional<Professor> buscarPorCpf(Cpf cpf) {
        return jpa.findByCpf(cpf.numero()).map(ProfessorEntity::paraDominio);
    }

    @Override
    public List<Professor> listar(boolean incluirInativos) {
        var lista = incluirInativos ? jpa.findAllByOrderByNome() : jpa.findByAtivoTrueOrderByNome();
        return lista.stream().map(ProfessorEntity::paraDominio).toList();
    }

    @Override
    public List<Professor> buscarPorTermo(String termo) {
        return jpa.buscar(termo, Termos.apenasDigitos(termo)).stream().map(ProfessorEntity::paraDominio).toList();
    }
}
