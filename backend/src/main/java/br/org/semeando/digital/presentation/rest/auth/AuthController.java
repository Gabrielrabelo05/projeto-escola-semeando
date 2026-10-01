package br.org.semeando.digital.presentation.rest.auth;

import br.org.semeando.digital.application.auth.AutenticacaoUseCase;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.presentation.rest.shared.Perfis;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

    public AuthController(AutenticacaoUseCase autenticacao) {
        this.autenticacao = autenticacao;
    }

    @PostMapping("/auth/login")
    public SessaoResponse login(@Valid @RequestBody LoginRequest request) {
        var sessao = autenticacao.entrar(request.email(), request.senha());
        return new SessaoResponse(sessao.token(), UsuarioResponse.de(sessao.usuario()));
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

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Perfis.ADMIN)
    public UsuarioResponse criar(@Valid @RequestBody NovoUsuarioRequest r, Ator ator) {
        return UsuarioResponse.de(autenticacao.criar(r.nome(), r.email(), r.senha(), r.perfil(), r.cpf(), ator));
    }

    @PatchMapping("/usuarios/{id}/inativar")
    @PreAuthorize(Perfis.ADMIN)
    public UsuarioResponse inativar(@PathVariable UUID id, Ator ator) {
        return UsuarioResponse.de(autenticacao.inativar(id, ator));
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String senha) {
    }

    public record NovoUsuarioRequest(@NotBlank String nome, @NotBlank @Email String email, @NotBlank String senha,
            @NotNull Perfil perfil, String cpf) {
    }

    public record SessaoResponse(String token, UsuarioResponse usuario) {
    }

    public record UsuarioResponse(UUID id, String nome, String email, Perfil perfil, boolean ativo) {

        static UsuarioResponse de(Usuario u) {
            return new UsuarioResponse(u.id(), u.nome(), u.email(), u.perfil(), u.ativo());
        }
    }
}
