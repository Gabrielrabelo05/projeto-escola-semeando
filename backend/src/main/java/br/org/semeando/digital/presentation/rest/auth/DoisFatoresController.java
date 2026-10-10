package br.org.semeando.digital.presentation.rest.auth;

import br.org.semeando.digital.application.auth.AutenticacaoUseCase.Etapa;
import br.org.semeando.digital.application.auth.AutenticacaoUseCase.Sessao;
import br.org.semeando.digital.application.auth.DoisFatoresUseCase;
import br.org.semeando.digital.application.auth.DoisFatoresUseCase.Ativacao;
import br.org.semeando.digital.application.auth.DoisFatoresUseCase.Configuracao;
import br.org.semeando.digital.application.auth.DoisFatoresUseCase.Situacao;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.infrastructure.security.CookieSessao;
import br.org.semeando.digital.presentation.rest.auth.AuthController.LoginResponse;
import br.org.semeando.digital.presentation.rest.auth.AuthController.UsuarioResponse;
import br.org.semeando.digital.presentation.rest.shared.Origem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Verificação em duas etapas (RNF-01).
 * <ul>
 * <li>{@code /auth/desafio/*}: etapa do login, depois da senha, com o cookie do desafio (sem sessão ainda);</li>
 * <li>{@code /auth/dois-fatores/*}: ativar ou desativar dentro do sistema, com a sessão.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/auth")
public class DoisFatoresController {

    private final DoisFatoresUseCase doisFatores;
    private final CookieSessao cookie;

    public DoisFatoresController(DoisFatoresUseCase doisFatores, CookieSessao cookie) {
        this.doisFatores = doisFatores;
        this.cookie = cookie;
    }

    @PostMapping("/desafio/codigo")
    public ResponseEntity<LoginResponse> confirmarLogin(@Valid @RequestBody CodigoRequest r, HttpServletRequest http) {
        Sessao sessao = doisFatores.confirmarLogin(cookie.lerDesafio(http), r.codigo(), Origem.ip(http));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.criar(sessao.token()).toString())
                .header(HttpHeaders.SET_COOKIE, cookie.apagarDesafio().toString())
                .body(new LoginResponse(Etapa.AUTENTICADO, UsuarioResponse.de(sessao.usuario())));
    }

    @PostMapping("/desafio/configuracao")
    public Configuracao prepararNoLogin(HttpServletRequest http) {
        return doisFatores.prepararNoLogin(cookie.lerDesafio(http));
    }

    @PostMapping("/desafio/ativacao")
    public ResponseEntity<AtivacaoResponse> ativarNoLogin(@Valid @RequestBody CodigoRequest r,
            HttpServletRequest http) {
        Ativacao ativacao = doisFatores.ativarNoLogin(cookie.lerDesafio(http), r.codigo(), Origem.ip(http));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.criar(ativacao.sessao().token()).toString())
                .header(HttpHeaders.SET_COOKIE, cookie.apagarDesafio().toString())
                .body(AtivacaoResponse.de(ativacao));
    }

    @GetMapping("/dois-fatores")
    public Situacao situacao(Ator ator) {
        return doisFatores.situacao(ator);
    }

    @PostMapping("/dois-fatores/configuracao")
    public Configuracao preparar(Ator ator) {
        return doisFatores.preparar(ator);
    }

    /** Ativar encerra as outras sessões; esta recebe uma sessão nova, já com o segundo fator. */
    @PostMapping("/dois-fatores/ativacao")
    public ResponseEntity<AtivacaoResponse> ativar(@Valid @RequestBody CodigoRequest r, Ator ator,
            HttpServletRequest http) {
        Ativacao ativacao = doisFatores.ativar(ator, r.codigo(), Origem.ip(http));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.criar(ativacao.sessao().token()).toString())
                .body(AtivacaoResponse.de(ativacao));
    }

    /** {@code sessaoEncerrada}: perfil obrigado a usar 2FA, que precisa entrar de novo e configurar outro aparelho. */
    @PostMapping("/dois-fatores/desativacao")
    public ResponseEntity<DesativacaoResponse> desativar(@Valid @RequestBody CodigoRequest r, Ator ator,
            HttpServletRequest http) {
        var sessao = doisFatores.desativar(ator, r.codigo(), Origem.ip(http));
        String novoCookie = sessao.map(s -> cookie.criar(s.token())).orElseGet(cookie::apagar).toString();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, novoCookie)
                .body(new DesativacaoResponse(sessao.isEmpty()));
    }

    public record CodigoRequest(@NotBlank String codigo) {
    }

    public record AtivacaoResponse(UsuarioResponse usuario, List<String> codigosRecuperacao) {

        static AtivacaoResponse de(Ativacao a) {
            return new AtivacaoResponse(UsuarioResponse.de(a.sessao().usuario()), a.codigosRecuperacao());
        }
    }

    public record DesativacaoResponse(boolean sessaoEncerrada) {
    }
}
