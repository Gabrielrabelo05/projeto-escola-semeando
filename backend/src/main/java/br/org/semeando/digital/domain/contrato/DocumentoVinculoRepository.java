package br.org.semeando.digital.domain.contrato;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentoVinculoRepository {

    DocumentoVinculo salvar(DocumentoVinculo documento);

    Optional<DocumentoVinculo> buscarPorId(UUID id);

    /** Versão vigente (não substituída) do vínculo de uma matrícula. */
    Optional<DocumentoVinculo> buscarVigentePorMatricula(UUID matriculaId);

    Optional<DocumentoVinculo> buscarVigentePorAlunoEAno(UUID alunoId, int anoLetivo);

    List<DocumentoVinculo> listarVersoes(UUID numero);

    List<DocumentoVinculo> pesquisar(UUID alunoId, Integer anoLetivo, StatusDocumentoVinculo status);
}
