package br.org.semeando.digital.infrastructure.persistence.pessoa;

import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.PrestadorServico;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prestadores")
class PrestadorEntity {

    @Id
    private UUID id;
    private String nome;
    private String documento;
    private String servicoPrestado;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private String email;
    private String telefone;
    private boolean ativo;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "prestador_documentos", joinColumns = @JoinColumn(name = "prestador_id"))
    @OrderColumn(name = "ordem")
    @Column(name = "descricao")
    private List<String> documentos = new ArrayList<>();

    protected PrestadorEntity() {
    }

    static PrestadorEntity de(PrestadorServico p) {
        PrestadorEntity e = new PrestadorEntity();
        e.id = p.id();
        e.nome = p.nome();
        e.documento = p.documento();
        e.servicoPrestado = p.servicoPrestado();
        e.vigenciaInicio = p.vigenciaInicio();
        e.vigenciaFim = p.vigenciaFim();
        e.email = p.contato().email();
        e.telefone = p.contato().telefone();
        e.ativo = p.ativo();
        e.documentos = new ArrayList<>(p.documentosObrigatorios());
        return e;
    }

    PrestadorServico paraDominio() {
        return new PrestadorServico(id, nome, documento, servicoPrestado, vigenciaInicio, vigenciaFim, documentos,
                new Contato(email, telefone), ativo);
    }
}
