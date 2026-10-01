package br.org.semeando.digital.infrastructure.persistence.turma;

import br.org.semeando.digital.domain.turma.Turma;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class TurmaRepositoryAdapter implements TurmaRepository {

    private static final Comparator<Turma> ORDEM = Comparator.comparing(Turma::anoLetivo).reversed()
            .thenComparing(Turma::serie).thenComparing(Turma::nome);

    private final TurmaJpaRepository jpa;

    TurmaRepositoryAdapter(TurmaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Turma salvar(Turma turma) {
        return jpa.save(TurmaEntity.de(turma)).paraDominio();
    }

    @Override
    public Optional<Turma> buscarPorId(UUID id) {
        return jpa.findById(id).map(TurmaEntity::paraDominio);
    }

    /** Ordenação pela ordem natural do enum (Maternal I → 5º ano), não alfabética. */
    @Override
    public List<Turma> listarPorAnoLetivo(int anoLetivo) {
        return jpa.findByAnoLetivoOrderBySerieAscNomeAsc(anoLetivo).stream().map(TurmaEntity::paraDominio)
                .sorted(ORDEM).toList();
    }

    @Override
    public List<Turma> listarTodas() {
        return jpa.findAllByOrderByAnoLetivoDescSerieAscNomeAsc().stream().map(TurmaEntity::paraDominio)
                .sorted(ORDEM).toList();
    }
}
