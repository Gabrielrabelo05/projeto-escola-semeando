package br.org.semeando.digital.domain.contrato;

import java.time.Instant;
import java.util.UUID;

/** Dados comuns a todo documento de vínculo – usado para (re)construir instâncias. */
public record DadosVinculo(UUID id, UUID numero, int versao, UUID alunoId, UUID matriculaId, int anoLetivo,
        Instant geradoEm, String geradoPor, String conteudo, String hashDocumento, StatusDocumentoVinculo status,
        AssinaturaEletronica assinatura) {
}
