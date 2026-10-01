package br.org.semeando.digital.infrastructure.persistence.matricula;

import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class MatriculaRepositoryAdapter implements MatriculaRepository {

    private static final EnumSet<StatusMatricula> OCUPAM_VAGA = EnumSet.of(StatusMatricula.APROVADA,
            StatusMatricula.ATIVA);

    private final MatriculaJpaRepository jpa;

    MatriculaRepositoryAdapter(MatriculaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Matricula salvar(Matricula matricula) {
        return jpa.save(MatriculaEntity.de(matricula)).paraDominio();
    }

    @Override
    public Optional<Matricula> buscarPorId(UUID id) {
        return jpa.findById(id).map(MatriculaEntity::paraDominio);
    }

    @Override
    public Optional<Matricula> buscarPorCobrancaTaxa(UUID cobrancaId) {
        return jpa.findByCobrancaTaxaId(cobrancaId).map(MatriculaEntity::paraDominio);
    }

    @Override
    public List<Matricula> listarPorAluno(UUID alunoId) {
        return jpa.findByAlunoId(alunoId).stream().map(MatriculaEntity::paraDominio).toList();
    }

    @Override
    public List<Matricula> listar(StatusMatricula status, Integer anoLetivo) {
        return jpa.filtrar(status, anoLetivo).stream().map(MatriculaEntity::paraDominio).toList();
    }

    @Override
    public long contarOcupandoVaga(UUID turmaId) {
        return jpa.countByTurmaIdAndStatusIn(turmaId, OCUPAM_VAGA);
    }

    @Override
    public List<UUID> alunosAtivosDaTurma(UUID turmaId) {
        return jpa.alunosDaTurma(turmaId, StatusMatricula.ATIVA);
    }
}
