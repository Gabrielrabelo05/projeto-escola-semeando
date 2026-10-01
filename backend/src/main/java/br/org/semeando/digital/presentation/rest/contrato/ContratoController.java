package br.org.semeando.digital.presentation.rest.contrato;

import br.org.semeando.digital.application.contrato.ContratosUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.contrato.StatusDocumentoVinculo;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.presentation.rest.contrato.ContratoDtos.ContratoResponse;
import br.org.semeando.digital.presentation.rest.contrato.ContratoDtos.GerarContratoRequest;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Módulo 2 – Contratos e Fichas de Sócio (RF-13 a RF-18). */
@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    private final ContratosUseCase contratos;

    public ContratoController(ContratosUseCase contratos) {
        this.contratos = contratos;
    }

    @GetMapping
    @PreAuthorize(Perfis.SECRETARIA)
    public List<ContratoResponse> pesquisar(@RequestParam(required = false) UUID alunoId,
            @RequestParam(required = false) UUID turmaId, @RequestParam(required = false) Integer anoLetivo,
            @RequestParam(required = false) StatusDocumentoVinculo status) {
        return contratos.pesquisar(alunoId, turmaId, anoLetivo, status).stream().map(ContratoResponse::de).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize(Perfis.SECRETARIA_OU_RESPONSAVEL)
    public ContratoResponse buscar(@PathVariable UUID id, Ator ator) {
        return ContratoResponse.de(contratos.buscar(id, ator));
    }

    @GetMapping("/{id}/versoes")
    @PreAuthorize(Perfis.SECRETARIA_OU_RESPONSAVEL)
    public List<ContratoResponse> versoes(@PathVariable UUID id, Ator ator) {
        return contratos.versoes(id, ator).stream().map(ContratoResponse::de).toList();
    }

    /** RF-17: geração com valores informados – bloqueada se divergir da tabela oficial (RN-02). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public ContratoResponse gerar(@Valid @RequestBody GerarContratoRequest request, Ator ator) {
        return ContratoResponse.de(contratos.gerar(request.matriculaId(), Dinheiro.de(request.valorParcela()),
                request.numeroParcelas(), ator));
    }

    @PostMapping("/{id}/nova-versao")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.SECRETARIA)
    public ContratoResponse novaVersao(@PathVariable UUID id, Ator ator) {
        return ContratoResponse.de(contratos.gerarNovaVersao(id, ator));
    }

    /** RF-15: assinatura eletrônica (registra IP de origem e hash). */
    @PostMapping("/{id}/assinar")
    @PreAuthorize(Perfis.SECRETARIA_OU_RESPONSAVEL)
    public ContratoResponse assinar(@PathVariable UUID id, HttpServletRequest http, Ator ator) {
        return ContratoResponse.de(contratos.assinar(id, enderecoIp(http), ator));
    }

    private static String enderecoIp(HttpServletRequest http) {
        String encaminhado = http.getHeader("X-Forwarded-For");
        return encaminhado == null || encaminhado.isBlank() ? http.getRemoteAddr()
                : encaminhado.split(",")[0].trim();
    }
}
