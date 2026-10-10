package br.org.semeando.digital.application.auth;

import br.org.semeando.digital.application.port.NotificacaoGateway;
import br.org.semeando.digital.application.port.NotificacaoGateway.Mensagem;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.usuario.ConviteAcesso;
import br.org.semeando.digital.domain.usuario.ConviteAcessoRepository;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;

/**
 * Cria o acesso de uma pessoa ao sistema sem que ninguém conheça a senha dela: gera um link de uso único para a
 * própria pessoa definir a senha, envia por e-mail e devolve o link para quem convidou repassar (ex.: WhatsApp).
 */
public class EmissorConvite {

    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final ConviteAcessoRepository convites;
    private final NotificacaoGateway notificacoes;
    private final String enderecoWeb;
    private final Clock clock;

    public EmissorConvite(UsuarioRepository usuarios, ConviteAcessoRepository convites,
            NotificacaoGateway notificacoes, String enderecoWeb, Clock clock) {
        this.usuarios = usuarios;
        this.convites = convites;
        this.notificacoes = notificacoes;
        this.enderecoWeb = enderecoWeb.replaceAll("/+$", "");
        this.clock = clock;
    }

    /** Cria o usuário (sem senha) ou, se o e-mail já tem acesso, gera um novo link para ele. */
    public Convite convidar(String nome, String email, Perfil perfil, Cpf cpf) {
        if (email == null || email.isBlank()) {
            throw new DomainException("EMAIL_OBRIGATORIO", "Informe o e-mail de " + nome + " para criar o acesso");
        }
        Usuario usuario = usuarios.buscarPorEmail(email.trim().toLowerCase())
                .orElseGet(() -> usuarios.salvar(Usuario.convidado(nome, email.trim(), perfil, cpf)));
        return novoLink(usuario);
    }

    /** Também serve para redefinir a senha de quem esqueceu. Links anteriores deixam de valer. */
    public Convite novoLink(Usuario usuario) {
        if (!usuario.ativo()) {
            throw new DomainException("USUARIO_INATIVO", "O usuário " + usuario.email() + " está inativo");
        }
        Instant agora = clock.instant();
        convites.listarPorUsuario(usuario.id()).stream()
                .filter(c -> c.validoEm(agora))
                .forEach(c -> {
                    c.revogar(agora);
                    convites.salvar(c);
                });
        String token = token();
        ConviteAcesso convite = convites.salvar(ConviteAcesso.emitir(usuario.id(), token, agora));
        String link = enderecoWeb + "/definir-senha?token=" + token;
        notificacoes.enviar(new Mensagem(usuario.nome(), usuario.email(), null, "Seu acesso ao Semeando Digital",
                """
                Olá, %s. Para entrar no Semeando Digital, defina sua senha neste link (válido por 7 dias e uma \
                única vez):
                %s
                Depois, entre com o e-mail %s.""".formatted(usuario.nome(), link, usuario.email())));
        return new Convite(usuario, link, convite.expiraEm());
    }

    private static String token() {
        byte[] bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record Convite(Usuario usuario, String link, Instant expiraEm) {
    }
}
