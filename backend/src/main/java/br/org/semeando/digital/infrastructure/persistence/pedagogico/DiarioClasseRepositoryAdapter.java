package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.DiarioClasse;
import br.org.semeando.digital.domain.pedagogico.DiarioClasseRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class DiarioClasseRepositoryAdapter implements DiarioClasseRepository {

    private final DiarioClasseJpaRepository jpa;

    DiarioClasseRepositoryAdapter(DiarioClasseJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public DiarioClasse salvar(DiarioClasse diario) {
        return jpa.save(DiarioClasseEntity.de(diario)).paraDominio();
    }

    @Override
    public Optional<DiarioClasse> buscarPorTurmaEData(UUID turmaId, LocalDate data) {
        return jpa.findByTurmaIdAndData(turmaId, data).map(DiarioClasseEntity::paraDominio);
    }

    @Override
    public List<DiarioClasse> listarPorTurma(UUID turmaId, LocalDate inicio, LocalDate fim) {
        return jpa.findByTurmaIdAndDataBetweenOrderByData(turmaId, inicio, fim).stream()
                .map(DiarioClasseEntity::paraDominio).toList();
    }

    @Override
    public List<DiarioClasse> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return jpa.findByDataBetweenOrderByData(inicio, fim).stream().map(DiarioClasseEntity::paraDominio).toList();
    }
}
