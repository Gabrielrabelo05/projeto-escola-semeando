package br.org.semeando.digital.infrastructure.persistence.aluno;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.infrastructure.persistence.shared.Termos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class AlunoRepositoryAdapter implements AlunoRepository {

    private final AlunoJpaRepository jpa;

    AlunoRepositoryAdapter(AlunoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Aluno salvar(Aluno aluno) {
        return jpa.save(AlunoEntity.de(aluno)).paraDominio();
    }

    @Override
    public Optional<Aluno> buscarPorId(UUID id) {
        return jpa.findById(id).map(AlunoEntity::paraDominio);
    }

    @Override
    public List<Aluno> listar(StatusAluno status) {
        var encontrados = status == null ? jpa.findAllByOrderByNome() : jpa.findByStatusOrderByNome(status);
        return encontrados.stream().map(AlunoEntity::paraDominio).toList();
    }

    @Override
    public List<Aluno> listarPorCpfResponsavel(Cpf cpf) {
        return jpa.findByCpfResponsavelOrderByNome(cpf.numero()).stream().map(AlunoEntity::paraDominio).toList();
    }

    @Override
    public List<Aluno> buscarPorTermo(String termo) {
        return jpa.buscar(termo, Termos.apenasDigitos(termo)).stream().map(AlunoEntity::paraDominio).toList();
    }
}
