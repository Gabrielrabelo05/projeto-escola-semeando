package br.org.semeando.digital.infrastructure.persistence.matricula;

import br.org.semeando.digital.domain.matricula.Farda;
import br.org.semeando.digital.domain.matricula.Matricula;
import br.org.semeando.digital.domain.matricula.StatusMatricula;
import br.org.semeando.digital.domain.matricula.TipoMatricula;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "matriculas")
class MatriculaEntity {

    @Id
    private UUID id;
    private UUID alunoId;
    private UUID turmaId;
    @Enumerated(EnumType.STRING)
    private NivelEnsino nivel;
    private int anoLetivo;
    @Enumerated(EnumType.STRING)
    private TipoMatricula tipo;
    @Enumerated(EnumType.STRING)
    private StatusMatricula status;
    private LocalDate dataSolicitacao;
    private String justificativaRejeicao;
    private String fardaBlusa;
    private String fardaCalca;
    private String fardaTenis;
    private UUID primeiraParcelaId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "matricula_documentos", joinColumns = @JoinColumn(name = "matricula_id"))
    @OrderColumn(name = "ordem")
    private List<DocumentoMatriculaEmbeddable> documentos = new ArrayList<>();

    protected MatriculaEntity() {
    }

    static MatriculaEntity de(Matricula m) {
        MatriculaEntity e = new MatriculaEntity();
        e.id = m.id();
        e.alunoId = m.alunoId();
        e.turmaId = m.turmaId();
        e.nivel = m.nivel();
        e.anoLetivo = m.anoLetivo();
        e.tipo = m.tipo();
        e.status = m.status();
        e.dataSolicitacao = m.dataSolicitacao();
        e.justificativaRejeicao = m.justificativaRejeicao();
        if (m.farda() != null) {
            e.fardaBlusa = m.farda().blusa();
            e.fardaCalca = m.farda().calca();
            e.fardaTenis = m.farda().tenis();
        }
        e.primeiraParcelaId = m.primeiraParcelaId();
        e.documentos = new ArrayList<>(m.documentos().stream().map(DocumentoMatriculaEmbeddable::de).toList());
        return e;
    }

    Matricula paraDominio() {
        Farda farda = fardaBlusa == null ? null : new Farda(fardaBlusa, fardaCalca, fardaTenis);
        return new Matricula(id, alunoId, turmaId, nivel, anoLetivo, tipo, status, dataSolicitacao,
                justificativaRejeicao, farda, documentos.stream().map(DocumentoMatriculaEmbeddable::paraDominio)
                        .toList(), primeiraParcelaId);
    }
}
