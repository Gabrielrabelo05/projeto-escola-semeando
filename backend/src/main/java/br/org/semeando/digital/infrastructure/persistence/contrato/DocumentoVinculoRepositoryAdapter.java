package br.org.semeando.digital.infrastructure.persistence.contrato;

import br.org.semeando.digital.domain.contrato.DocumentoVinculo;
import br.org.semeando.digital.domain.contrato.DocumentoVinculoRepository;
import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class DocumentoVinculoRepositoryAdapter implements DocumentoVinculoRepository {

    private final DocumentoVinculoJpaRepository jpa;

    DocumentoVinculoRepositoryAdapter(DocumentoVinculoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public DocumentoVinculo salvar(DocumentoVinculo documento) {
        return jpa.save(DocumentoVinculoEntity.de(documento)).paraDominio();
    }

    @Override
    public Optional<DocumentoVinculo> buscarPorId(UUID id) {
        return jpa.findById(id).map(DocumentoVinculoEntity::paraDominio);
    }

    @Override
    public Optional<DocumentoVinculo> buscarVigentePorMatricula(UUID matriculaId) {
        return jpa.findFirstByMatriculaIdAndStatusNotOrderByVersaoDesc(matriculaId, StatusDocumentoVinculo.SUBSTITUIDO)
                .map(DocumentoVinculoEntity::paraDominio);
    }

    @Override
    public Optional<DocumentoVinculo> buscarVigentePorAlunoEAno(UUID alunoId, int anoLetivo) {
        return jpa.findFirstByAlunoIdAndAnoLetivoAndStatusNotOrderByGeradoEmDesc(alunoId, anoLetivo,
                StatusDocumentoVinculo.SUBSTITUIDO).map(DocumentoVinculoEntity::paraDominio);
    }

    @Override
    public List<DocumentoVinculo> listarVersoes(UUID numero) {
        return jpa.findByNumero(numero).stream().map(DocumentoVinculoEntity::paraDominio).toList();
    }

    @Override
    public List<DocumentoVinculo> pesquisar(UUID alunoId, Integer anoLetivo, StatusDocumentoVinculo status) {
        return jpa.pesquisar(alunoId, anoLetivo, status).stream().map(DocumentoVinculoEntity::paraDominio).toList();
    }
}
