package br.org.semeando.digital.application.auth;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.auth.EmissorConvite.Convite;
import br.org.semeando.digital.application.port.CodificadorSenha;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.usuario.ConviteAcesso;
import br.org.semeando.digital.domain.usuario.ConviteAcessoRepository;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.domain.usuario.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/** Acesso ao sistema por convite: quem é cadastrado recebe um link e define a própria senha (RF-35). */
@CasoDeUso
public class ConvitesAcessoUseCase {

    private final UsuarioRepository usuarios;
    private final ConviteAcessoRepository convites;
    private final EmissorConvite emissor;
    private final CodificadorSenha codificador;
    private final Auditor auditor;
    private final Clock clock;

    public ConvitesAcessoUseCase(UsuarioRepository usuarios, ConviteAcessoRepository convites,
            EmissorConvite emissor, CodificadorSenha codificador, Auditor auditor, Clock clock) {
        this.usuarios = usuarios;
        this.convites = convites;
        this.emissor = emissor;
        this.codificador = codificador;
        this.auditor = auditor;
        this.clock = clock;
    }

    public Convite convidar(String nome, String email, Perfil perfil, String cpf, Ator ator) {
        Cpf documento = cpf == null || cpf.isBlank() ? null : Cpf.de(cpf);
        if ((perfil == Perfil.PROFESSOR || perfil == Perfil.RESPONSAVEL) && documento == null) {
            throw new DomainException("CPF_OBRIGATORIO",
                    "Professores e responsáveis precisam de CPF (vínculo com o cadastro)");
        }
        Convite convite = emissor.convidar(nome, email, perfil, documento);
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Usuario", convite.usuario().id(),
                "Link de acesso para " + convite.usuario().email() + " " + convite.usuario().perfil());
        return convite;
    }

    public Convite novoLink(UUID usuarioId, Ator ator) {
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new DomainException("NAO_ENCONTRADO", "Usuário não encontrado"));
        Convite convite = emissor.novoLink(usuario);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Usuario", usuarioId, "Novo link de acesso");
        return convite;
    }

    /** Dados exibidos na página pública de definição de senha. */
    public Usuario consultar(String token) {
        ConviteAcesso convite = convite(token);
        convite.garantirValido(clock.instant());
        return usuarios.buscarPorId(convite.usuarioId()).orElseThrow();
    }

    public Usuario definirSenha(String token, String senha) {
        AutenticacaoUseCase.validarSenha(senha);
        Instant agora = clock.instant();
        ConviteAcesso convite = convite(token);
        convite.usar(agora);
        Usuario usuario = usuarios.buscarPorId(convite.usuarioId()).orElseThrow();
        usuario.alterarSenha(codificador.codificar(senha));
        convites.salvar(convite);
        Usuario salvo = usuarios.salvar(usuario);
        auditor.registrar(new Ator(usuario.id(), usuario.nome(), usuario.perfil(), usuario.cpf()),
                AcaoAuditada.EDICAO, "Usuario", usuario.id(), "Senha definida pelo link de acesso");
        return salvo;
    }

    private ConviteAcesso convite(String token) {
        return convites.buscarPorTokenHash(ConviteAcesso.hash(token == null ? "" : token))
                .orElseThrow(() -> new DomainException("CONVITE_INVALIDO",
                        "Link de acesso inválido. Peça um novo link à secretaria."));
    }
}
