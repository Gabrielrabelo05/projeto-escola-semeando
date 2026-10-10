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
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Login e gestão de usuários (perfil Administrador – RF-35). */
@CasoDeUso
public class AutenticacaoUseCase {

    private static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepository usuarios;
    private final CodificadorSenha codificador;
    private final EmissorToken emissorToken;
    private final LimitadorTentativasLogin limitador;
    private final Auditor auditor;
    private final Clock clock;
    /** Hash usado quando o e-mail não existe, para o tempo de resposta não revelar quem tem conta. */
    private final String hashFicticio;

    public AutenticacaoUseCase(UsuarioRepository usuarios, CodificadorSenha codificador, EmissorToken emissorToken,
            LimitadorTentativasLogin limitador, Auditor auditor, Clock clock) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emissorToken = emissorToken;
        this.limitador = limitador;
        this.auditor = auditor;
        this.clock = clock;
        this.hashFicticio = codificador.codificar("senha-ficticia-para-tempo-constante");
    }

    /** {@code origem}: IP de quem tenta entrar, usado no bloqueio por força bruta. */
    public Sessao entrar(String email, String senha, String origem) {
        String normalizado = email == null ? "" : email.trim().toLowerCase();
        limitador.verificar(normalizado, origem);
        Optional<Usuario> encontrado = usuarios.buscarPorEmail(normalizado);
        String hash = encontrado.filter(Usuario::possuiSenha).map(Usuario::senhaHash).orElse(hashFicticio);
        boolean senhaConfere = codificador.confere(senha == null ? "" : senha, hash);
        Usuario usuario = encontrado.filter(Usuario::ativo).filter(Usuario::possuiSenha)
                .filter(u -> senhaConfere)
                .orElseThrow(() -> {
                    limitador.registrarFalha(normalizado, origem);
                    return new DomainException("CREDENCIAIS_INVALIDAS", "E-mail ou senha inválidos");
                });
        limitador.registrarSucesso(normalizado, origem);
        return new Sessao(emissorToken.emitir(usuario), usuario);
    }

    /** Usuário da sessão atual, lido do banco (nome e perfil sempre atualizados). */
    public Usuario usuarioAtual(UUID id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new DomainException("NAO_ENCONTRADO", "Usuário não encontrado"));
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
        usuario.inativar(clock.instant());
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Usuario", id, usuario.email());
        return usuarios.salvar(usuario);
    }

    public record Sessao(String token, Usuario usuario) {
    }
}
