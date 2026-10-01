package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Responsavel;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.infrastructure.persistence.shared.Termos;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class ResponsavelRepositoryAdapter implements ResponsavelRepository {

    private final ResponsavelJpaRepository jpa;

    ResponsavelRepositoryAdapter(ResponsavelJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Responsavel salvar(Responsavel entidade) {
        return jpa.save(ResponsavelEntity.de(entidade)).paraDominio();
    }

    @Override
    public Optional<Responsavel> buscarPorId(UUID id) {
        return jpa.findById(id).map(ResponsavelEntity::paraDominio);
    }

    @Override
    public Optional<Responsavel> buscarPorCpf(Cpf cpf) {
        return jpa.findByCpf(cpf.numero()).map(ResponsavelEntity::paraDominio);
    }

    @Override
    public List<Responsavel> listar(boolean incluirInativos) {
        var lista = incluirInativos ? jpa.findAllByOrderByNome() : jpa.findByAtivoTrueOrderByNome();
        return lista.stream().map(ResponsavelEntity::paraDominio).toList();
    }

    @Override
    public List<Responsavel> buscarPorTermo(String termo) {
        return jpa.buscar(termo, Termos.apenasDigitos(termo)).stream().map(ResponsavelEntity::paraDominio).toList();
    }
}
