package br.org.semeando.digital.presentation.rest.portal;

import br.org.semeando.digital.application.portal.PortalResponsavelUseCase;
import br.org.semeando.digital.application.portal.PortalResponsavelUseCase.ResumoFilho;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.presentation.rest.cobranca.CobrancaDtos.CobrancaResponse;
import br.org.semeando.digital.presentation.rest.contrato.ContratoDtos.ContratoResponse;
import br.org.semeando.digital.presentation.rest.shared.Dtos.AlunoResumo;
import br.org.semeando.digital.presentation.rest.shared.Dtos.TurmaDto;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Portal do responsável (leitura): filhos, boletos, presença e documentos escolares. */
@RestController
@RequestMapping("/api/portal")
@PreAuthorize(Perfis.RESPONSAVEL)
public class PortalController {

    private final PortalResponsavelUseCase portal;
    private final Clock clock;

    public PortalController(PortalResponsavelUseCase portal, Clock clock) {
        this.portal = portal;
        this.clock = clock;
    }

    @GetMapping("/filhos")
    public List<FilhoResponse> filhos(Ator ator) {
        return portal.meusFilhos(ator).stream().map(FilhoResponse::de).toList();
    }

    @GetMapping("/filhos/{alunoId}/boletos")
    public List<CobrancaResponse> boletos(@PathVariable UUID alunoId, Ator ator) {
        return portal.boletos(alunoId, ator).stream().map(c -> CobrancaResponse.de(c, clock)).toList();
    }

    @GetMapping("/filhos/{alunoId}/documentos")
    public List<ContratoResponse> documentos(@PathVariable UUID alunoId, Ator ator) {
        return portal.documentos(alunoId, ator).stream().map(ContratoResponse::de).toList();
    }

    public record FilhoResponse(AlunoResumo aluno, UUID matriculaId, String statusMatricula, TurmaDto turma,
            int aulas, int faltas, BigDecimal percentualPresenca, boolean alertaFrequencia) {

        static FilhoResponse de(ResumoFilho r) {
            var f = r.frequencia();
            return new FilhoResponse(AlunoResumo.de(r.aluno()), r.matricula() == null ? null : r.matricula().id(),
                    r.matricula() == null ? null : r.matricula().status().name(), TurmaDto.de(r.turma()), f.aulas(),
                    f.faltas(), f.percentual(), f.emAlerta());
        }
    }
}
