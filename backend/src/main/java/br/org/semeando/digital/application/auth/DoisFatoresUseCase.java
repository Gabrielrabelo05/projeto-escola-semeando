package br.org.semeando.digital.application.auth;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.auth.AutenticacaoUseCase.Sessao;
import br.org.semeando.digital.application.port.EmissorToken;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.usuario.ConviteAcesso;
import br.org.semeando.digital.domain.usuario.PoliticaDoisFatores;
import br.org.semeando.digital.domain.usuario.Totp;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

/**
 * Verificação em duas etapas por aplicativo autenticador (RNF-01). Depois da senha, o login pede o código de 6
 * dígitos do aplicativo, ou um código de recuperação de uso único. Errar o código 5 vezes bloqueia por 15 minutos.
 */
@CasoDeUso
public class DoisFatoresUseCase {

    static final String EMISSOR = "Semeando Digital";
    static final int QUANTIDADE_CODIGOS_RECUPERACAO = 10;
    /** Sem 0/O, 1/I/L: fáceis de confundir ao digitar. */
    private static final String ALFABETO_RECUPERACAO = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int BYTES_SEGREDO = 20;

    private final UsuarioRepository usuarios;
    private final EmissorToken emissorToken;
    private final LimitadorTentativasLogin limitador;
    private final PoliticaDoisFatores politica;
    private final Auditor auditor;
    private final Clock clock;
    private final SecureRandom aleatorio = new SecureRandom();

    public DoisFatoresUseCase(UsuarioRepository usuarios, EmissorToken emissorToken,
            LimitadorTentativasLogin limitador, PoliticaDoisFatores politica, Auditor auditor, Clock clock) {
        this.usuarios = usuarios;
        this.emissorToken = emissorToken;
        this.limitador = limitador;
        this.politica = politica;
        this.auditor = auditor;
        this.clock = clock;
    }

    // Etapa do login (só com o desafio emitido depois da senha)

    public Sessao confirmarLogin(String tokenDesafio, String codigo, String origem) {
        Usuario usuario = usuarioDoDesafio(tokenDesafio);
        if (!usuario.doisFatoresAtivo()) {
            throw desafioExpirado();
        }
        conferir(usuario, codigo, origem);
        return new Sessao(emissorToken.emitir(usuario, true), usuario);
    }

    /** 1º acesso de quem é obrigado a usar 2FA: gera o QR Code antes de abrir a sessão. */
    public Configuracao prepararNoLogin(String tokenDesafio) {
        return preparar(usuarioDoDesafio(tokenDesafio));
    }

    public Ativacao ativarNoLogin(String tokenDesafio, String codigo, String origem) {
        return ativar(usuarioDoDesafio(tokenDesafio), codigo, origem);
    }

    // Dentro do sistema (tela "Minha conta")

    public Situacao situacao(Ator ator) {
        Usuario usuario = usuario(ator.usuarioId());
        return new Situacao(usuario.doisFatoresAtivo(), politica.obrigatorio(usuario.perfil()),
                usuario.doisFatores().codigosRecuperacao().size());
    }

    public Configuracao preparar(Ator ator) {
        return preparar(usuario(ator.usuarioId()));
    }

    public Ativacao ativar(Ator ator, String codigo, String origem) {
        return ativar(usuario(ator.usuarioId()), codigo, origem);
    }

    /**
     * Exige um código válido, para que uma sessão esquecida aberta não baste. Encerra as sessões; quem é obrigado a
     * usar 2FA precisa entrar de novo e configurar o novo aparelho, os demais seguem com uma sessão nova.
     */
    public Optional<Sessao> desativar(Ator ator, String codigo, String origem) {
        Usuario usuario = usuario(ator.usuarioId());
        if (!usuario.doisFatoresAtivo()) {
            throw new DomainException("DOIS_FATORES_INATIVO", "A verificação em duas etapas não está ativa.");
        }
        conferir(usuario, codigo, origem);
        usuario.desativarDoisFatores(clock.instant());
        Usuario salvo = usuarios.salvar(usuario);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Usuario", salvo.id(), "Verificação em duas etapas desativada");
        return politica.exige(salvo) ? Optional.empty()
                : Optional.of(new Sessao(emissorToken.emitir(salvo, false), salvo));
    }

    private Configuracao preparar(Usuario usuario) {
        byte[] bytes = new byte[BYTES_SEGREDO];
        aleatorio.nextBytes(bytes);
        String segredo = Totp.base32(bytes);
        usuario.prepararDoisFatores(segredo);
        usuarios.salvar(usuario);
        return new Configuracao(segredo, Totp.uri(EMISSOR, usuario.email(), segredo));
    }

    private Ativacao ativar(Usuario usuario, String codigo, String origem) {
        String chave = chaveLimite(usuario);
        limitador.verificar(chave, origem);
        Instant agora = clock.instant();
        OptionalLong passo = usuario.doisFatoresAtivo() ? OptionalLong.empty()
                : usuario.conferirCodigoTotp(codigo, agora);
        if (passo.isEmpty()) {
            limitador.registrarFalha(chave, origem);
            throw codigoInvalido();
        }
        limitador.registrarSucesso(chave, origem);
        List<String> codigos = novosCodigosRecuperacao();
        usuario.ativarDoisFatores(passo.getAsLong(), codigos.stream().map(DoisFatoresUseCase::hash).toList(), agora);
        Usuario salvo = usuarios.salvar(usuario);
        auditor.registrar(ator(salvo), AcaoAuditada.EDICAO, "Usuario", salvo.id(),
                "Verificação em duas etapas ativada");
        return new Ativacao(new Sessao(emissorToken.emitir(salvo, true), salvo), codigos);
    }

    /** Código do aplicativo ou de recuperação; o uso de um código de recuperação fica na auditoria. */
    private void conferir(Usuario usuario, String codigo, String origem) {
        String chave = chaveLimite(usuario);
        limitador.verificar(chave, origem);
        Instant agora = clock.instant();
        String digitado = codigo == null ? "" : codigo.trim();
        boolean doAplicativo = usuario.conferirCodigoTotp(digitado.replace(" ", ""), agora).isPresent();
        boolean deRecuperacao = !doAplicativo && usuario.usarCodigoRecuperacao(hash(digitado));
        if (!doAplicativo && !deRecuperacao) {
            limitador.registrarFalha(chave, origem);
            throw codigoInvalido();
        }
        limitador.registrarSucesso(chave, origem);
        usuarios.salvar(usuario);
        if (deRecuperacao) {
            auditor.registrar(ator(usuario), AcaoAuditada.EDICAO, "Usuario", usuario.id(),
                    "Login com código de recuperação (restam " + usuario.doisFatores().codigosRecuperacao().size()
                            + ")");
        }
    }

    private Usuario usuarioDoDesafio(String token) {
        if (token == null || token.isBlank()) {
            throw desafioExpirado();
        }
        return emissorToken.lerDesafio(token)
                .flatMap(d -> usuarios.buscarPorId(d.usuarioId()).filter(u -> u.aceitaSessaoEmitidaEm(d.emitidoEm())))
                .orElseThrow(DoisFatoresUseCase::desafioExpirado);
    }

    private Usuario usuario(UUID id) {
        return usuarios.buscarPorId(id)
                .orElseThrow(() -> new DomainException("NAO_ENCONTRADO", "Usuário não encontrado"));
    }

    private List<String> novosCodigosRecuperacao() {
        List<String> codigos = new ArrayList<>();
        for (int i = 0; i < QUANTIDADE_CODIGOS_RECUPERACAO; i++) {
            StringBuilder codigo = new StringBuilder();
            for (int c = 0; c < 10; c++) {
                if (c == 5) {
                    codigo.append('-');
                }
                codigo.append(ALFABETO_RECUPERACAO.charAt(aleatorio.nextInt(ALFABETO_RECUPERACAO.length())));
            }
            codigos.add(codigo.toString());
        }
        return codigos;
    }

    /** Ignora hífen, espaços e maiúsculas/minúsculas ao digitar o código de recuperação. */
    static String hash(String codigoRecuperacao) {
        return ConviteAcesso.hash(codigoRecuperacao.replace("-", "").replace(" ", "").toUpperCase());
    }

    private static String chaveLimite(Usuario usuario) {
        return "2fa:" + usuario.id();
    }

    private static Ator ator(Usuario usuario) {
        return new Ator(usuario.id(), usuario.nome(), usuario.perfil(), usuario.cpf());
    }

    private static DomainException codigoInvalido() {
        return new DomainException("CODIGO_INVALIDO", "Código inválido. Confira o aplicativo e tente novamente.");
    }

    private static DomainException desafioExpirado() {
        return new DomainException("DESAFIO_EXPIRADO", "A verificação expirou. Entre com sua senha novamente.");
    }

    /** {@code uri}: conteúdo do QR Code ({@code otpauth://}); {@code segredo}: para digitar no aplicativo. */
    public record Configuracao(String segredo, String uri) {
    }

    /** Os códigos de recuperação aparecem só aqui, uma vez; no banco ficam apenas os hashes. */
    public record Ativacao(Sessao sessao, List<String> codigosRecuperacao) {
    }

    public record Situacao(boolean ativo, boolean obrigatorio, int codigosRecuperacaoRestantes) {
    }
}
