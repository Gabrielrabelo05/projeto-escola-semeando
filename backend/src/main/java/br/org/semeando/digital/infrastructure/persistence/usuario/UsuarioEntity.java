package br.org.semeando.digital.infrastructure.persistence.usuario;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.usuario.DoisFatores;
import br.org.semeando.digital.domain.usuario.Perfil;
import br.org.semeando.digital.domain.usuario.Usuario;
import br.org.semeando.digital.infrastructure.persistence.shared.DadoSensivelConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
class UsuarioEntity {

    @Id
    private UUID id;
    private String nome;
    private String email;
    private String senhaHash;
    @Enumerated(EnumType.STRING)
    private Perfil perfil;
    private String cpf;
    private boolean ativo;
    private Instant sessoesValidasDesde;
    @Convert(converter = DadoSensivelConverter.class)
    private String doisFatoresSegredo;
    private boolean doisFatoresAtivo;
    private Long doisFatoresUltimoPasso;
    private String codigosRecuperacao;

    protected UsuarioEntity() {
    }

    static UsuarioEntity de(Usuario u) {
        UsuarioEntity e = new UsuarioEntity();
        e.id = u.id();
        e.nome = u.nome();
        e.email = u.email();
        e.senhaHash = u.senhaHash();
        e.perfil = u.perfil();
        e.cpf = u.cpf() == null ? null : u.cpf().numero();
        e.ativo = u.ativo();
        e.sessoesValidasDesde = u.sessoesValidasDesde();
        DoisFatores dois = u.doisFatores();
        e.doisFatoresSegredo = dois.segredo();
        e.doisFatoresAtivo = dois.ativo();
        e.doisFatoresUltimoPasso = dois.ultimoPasso();
        e.codigosRecuperacao = dois.codigosRecuperacao().isEmpty() ? null
                : String.join(",", dois.codigosRecuperacao());
        return e;
    }

    Usuario paraDominio() {
        return new Usuario(id, nome, email, senhaHash, perfil, cpf == null ? null : Cpf.de(cpf), ativo,
                sessoesValidasDesde, new DoisFatores(doisFatoresSegredo, doisFatoresAtivo, doisFatoresUltimoPasso,
                        codigosRecuperacao == null ? List.of() : Arrays.asList(codigosRecuperacao.split(","))));
    }
}
