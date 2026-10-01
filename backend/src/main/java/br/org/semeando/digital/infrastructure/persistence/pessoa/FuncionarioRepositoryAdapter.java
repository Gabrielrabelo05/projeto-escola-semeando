package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Funcionario;
import br.org.semeando.digital.domain.pessoa.FuncionarioRepository;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.infrastructure.persistence.shared.Termos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class FuncionarioRepositoryAdapter implements FuncionarioRepository {

    private final FuncionarioJpaRepository jpa;

    FuncionarioRepositoryAdapter(FuncionarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Funcionario salvar(Funcionario entidade) {
        return jpa.save(FuncionarioEntity.de(entidade)).paraDominio();
    }

    @Override
    public Optional<Funcionario> buscarPorId(UUID id) {
        return jpa.findById(id).map(FuncionarioEntity::paraDominio);
    }

    @Override
    public Optional<Funcionario> buscarPorCpf(Cpf cpf) {
        return jpa.findByCpf(cpf.numero()).map(FuncionarioEntity::paraDominio);
    }

    @Override
    public List<Funcionario> listar(boolean incluirInativos) {
        var lista = incluirInativos ? jpa.findAllByOrderByNome() : jpa.findByAtivoTrueOrderByNome();
        return lista.stream().map(FuncionarioEntity::paraDominio).toList();
    }

    @Override
    public List<Funcionario> buscarPorTermo(String termo) {
        return jpa.buscar(termo, Termos.apenasDigitos(termo)).stream().map(FuncionarioEntity::paraDominio).toList();
    }
}
