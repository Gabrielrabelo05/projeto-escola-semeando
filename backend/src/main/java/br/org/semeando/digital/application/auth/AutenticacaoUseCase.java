package br.org.semeando.digital.application.auth;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.port.CodificadorSenha;
import br.org.semeando.digital.application.port.EmissorToken;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import java.util.List;
import java.util.UUID;

/** Login e gestão de usuários (perfil Administrador – RF-35). */
@CasoDeUso
public class AutenticacaoUseCase {

    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarios;
    private final CodificadorSenha codificador;
    private final EmissorToken emissorToken;
    private final Auditor auditor;

    public AutenticacaoUseCase(UsuarioRepository usuarios, CodificadorSenha codificador, EmissorToken emissorToken,
            Auditor auditor) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emissorToken = emissorToken;
        this.auditor = auditor;
    }

    public Sessao entrar(String email, String senha) {
        Usuario usuario = usuarios.buscarPorEmail(email == null ? "" : email.trim().toLowerCase())
                .filter(Usuario::ativo)
                .filter(Usuario::possuiSenha)
                .filter(u -> codificador.confere(senha, u.senhaHash()))
                .orElseThrow(() -> new DomainException("CREDENCIAIS_INVALIDAS", "E-mail ou senha inválidos"));
        return new Sessao(emissorToken.emitir(usuario), usuario);
    }

    public List<Usuario> listar() {
        return usuarios.listar();
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new DomainException("SENHA_FRACA", "A senha deve ter ao menos " + TAMANHO_MINIMO_SENHA
                    + " caracteres");
        }
    }

    /** Usuário com senha já definida (contas de demonstração do seed). Pela API, o acesso é por convite. */
    public Usuario criar(String nome, String email, String senha, Perfil perfil, String cpf, Ator ator) {
        validarSenha(senha);
        if (usuarios.buscarPorEmail(email.trim().toLowerCase()).isPresent()) {
            throw new DomainException("DUPLICADO", "E-mail já cadastrado");
        }
        Cpf documento = cpf == null || cpf.isBlank() ? null : Cpf.de(cpf);
        Usuario usuario = usuarios.salvar(Usuario.novo(nome, email, codificador.codificar(senha), perfil, documento));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Usuario", usuario.id(), usuario.email() + " " + perfil);
        return usuario;
    }

    public Usuario inativar(UUID id, Ator ator) {
        Usuario usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new DomainException("NAO_ENCONTRADO", "Usuário não encontrado"));
        usuario.inativar();
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Usuario", id, usuario.email());
        return usuarios.salvar(usuario);
    }

    public record Sessao(String token, Usuario usuario) {
    }
}
