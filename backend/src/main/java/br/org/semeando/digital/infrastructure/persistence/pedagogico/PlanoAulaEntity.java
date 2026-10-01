package br.org.semeando.digital.infrastructure.persistence.pedagogico;

import br.org.semeando.digital.domain.pedagogico.ConteudoPlano;
import br.org.semeando.digital.domain.pedagogico.PlanoAula;
import br.org.semeando.digital.domain.pedagogico.StatusPlanoAula;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "planos_aula")
class PlanoAulaEntity {

    @Id
    private UUID id;
    private UUID professorId;
    private UUID turmaId;
    private LocalDate data;
    private String objetivo;
    private String conteudo;
    private String metodologia;
    private String avaliacao;
    private String recursos;
    @Enumerated(EnumType.STRING)
    private StatusPlanoAula status;
    private String comentarioCoordenacao;

    protected PlanoAulaEntity() {
    }

    static PlanoAulaEntity de(PlanoAula p) {
        PlanoAulaEntity e = new PlanoAulaEntity();
        e.id = p.id();
        e.professorId = p.professorId();
        e.turmaId = p.turmaId();
        e.data = p.data();
        e.objetivo = p.conteudo().objetivo();
        e.conteudo = p.conteudo().conteudo();
        e.metodologia = p.conteudo().metodologia();
        e.avaliacao = p.conteudo().avaliacao();
        e.recursos = p.conteudo().recursos();
        e.status = p.status();
        e.comentarioCoordenacao = p.comentarioCoordenacao();
        return e;
    }

    PlanoAula paraDominio() {
        return new PlanoAula(id, professorId, turmaId, data,
                new ConteudoPlano(objetivo, conteudo, metodologia, avaliacao, recursos), status, comentarioCoordenacao);
    }
}
