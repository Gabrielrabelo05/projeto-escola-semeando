package br.org.semeando.digital.presentation.rest.auth;

import br.org.semeando.digital.application.auth.AutenticacaoUseCase;
import br.org.semeando.digital.application.auth.AutenticacaoUseCase.Etapa;
import br.org.semeando.digital.application.auth.ConvitesAcessoUseCase;
import br.org.semeando.digital.application.auth.EmissorConvite.Convite;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.infrastructure.security.CookieSessao;
import br.org.semeando.digital.presentation.rest.shared.Origem;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AutenticacaoUseCase autenticacao;
    private final ConvitesAcessoUseCase convites;
    private final CookieSessao cookie;

    public AuthController(AutenticacaoUseCase autenticacao, ConvitesAcessoUseCase convites, CookieSessao cookie) {
        this.autenticacao = autenticacao;
        this.convites = convites;
        this.cookie = cookie;
    }

    /**
     * O token vai apenas no cookie HttpOnly; o corpo traz só os dados do usuário. Com verificação em duas etapas, a
     * resposta indica a próxima etapa e o cookie é o do desafio, que não abre sessão.
     */
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        var resultado = autenticacao.entrar(request.email(), request.senha(), Origem.ip(http));
        if (resultado.etapa() != Etapa.AUTENTICADO) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.criarDesafio(resultado.tokenDesafio()).toString())
                    .body(new LoginResponse(resultado.etapa(), null));
        }
        var sessao = resultado.sessao();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.criar(sessao.token()).toString())
                .body(new LoginResponse(Etapa.AUTENTICADO, UsuarioResponse.de(sessao.usuario())));
    }

    /** Usuário logado (o front consulta ao abrir, já que não tem acesso ao token). */
    @GetMapping("/auth/sessao")
    public UsuarioResponse sessao(Ator ator) {
        return UsuarioResponse.de(autenticacao.usuarioAtual(ator.usuarioId()));
    }

    @PostMapping("/auth/sair")
    public ResponseEntity<Void> sair() {
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.apagar().toString()).build();
    }

    @GetMapping("/auth/eu")
    public Ator eu(Ator ator) {
        return ator;
    }

    @GetMapping("/usuarios")
    @PreAuthorize(Perfis.ADMIN)
    public List<UsuarioResponse> listar() {
        return autenticacao.listar().stream().map(UsuarioResponse::de).toList();
    }

    /** Cria o acesso sem senha e devolve o link para a pessoa definir a própria senha. */
    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.ADMIN)
    public ConviteResponse criar(@Valid @RequestBody NovoUsuarioRequest r, Ator ator) {
        return ConviteResponse.de(convites.convidar(r.nome(), r.email(), r.perfil(), r.cpf(), ator));
    }

    /** Novo link de acesso: reenvio do convite ou redefinição de senha. */
    @PostMapping("/usuarios/{id}/link-acesso")
    @PreAuthorize(Perfis.ADMIN)
    public ConviteResponse novoLink(@PathVariable UUID id, Ator ator) {
        return ConviteResponse.de(convites.novoLink(id, ator));
    }

    @PatchMapping("/usuarios/{id}/inativar")
    @PreAuthorize(Perfis.ADMIN)
    public UsuarioResponse inativar(@PathVariable UUID id, Ator ator) {
        return UsuarioResponse.de(autenticacao.inativar(id, ator));
    }

    @GetMapping("/publico/convites/{token}")
    public UsuarioResponse convite(@PathVariable String token) {
        return UsuarioResponse.de(convites.consultar(token));
    }

    @PostMapping("/publico/convites/{token}/senha")
    public UsuarioResponse definirSenha(@PathVariable String token, @Valid @RequestBody DefinirSenhaRequest r) {
        return UsuarioResponse.de(convites.definirSenha(token, r.senha()));
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {
    }

    public record NovoUsuarioRequest(@NotBlank String nome, @NotBlank @Email String email, @NotNull Perfil perfil,
            String cpf) {
    }

    public record DefinirSenhaRequest(@NotBlank String senha) {
    }

    /** {@code usuario} só quando a etapa é AUTENTICADO. */
    public record LoginResponse(Etapa etapa, UsuarioResponse usuario) {
    }

    /**
     * {@code acessoPendente}: o usuário ainda não definiu a senha pelo link. {@code doisFatoresAtivo}: entra com o
     * código do aplicativo autenticador.
     */
    public record UsuarioResponse(UUID id, String nome, String email, Perfil perfil, boolean ativo,
            boolean acessoPendente, boolean doisFatoresAtivo) {

        public static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.id(), u.nome(), u.email(), u.perfil(), u.ativo(), !u.possuiSenha(),
                    u.doisFatoresAtivo());
        }
    }

    public record ConviteResponse(UsuarioResponse usuario, String link, Instant expiraEm) {

        public static ConviteResponse de(Convite c) {
            return new ConviteResponse(UsuarioResponse.de(c.usuario()), c.link(), c.expiraEm());
        }
    }
}
