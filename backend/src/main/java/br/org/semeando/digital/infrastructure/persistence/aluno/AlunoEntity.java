package br.org.semeando.digital.infrastructure.persistence.aluno;

import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AtividadeExtracurricular;
import br.org.semeando.digital.domain.aluno.InformacoesMedicas;
import br.org.semeando.digital.domain.aluno.StatusAluno;
import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.infrastructure.persistence.shared.AutorizadoEmbeddable;
import br.org.semeando.digital.infrastructure.persistence.shared.DadoSensivelConverter;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "alunos")
class AlunoEntity {

    @Id
    private UUID id;
    private String nome;
    private LocalDate dataNascimento;
    private UUID responsavelId;
    private String cpfResponsavel;
    @Convert(converter = DadoSensivelConverter.class)
    private String alergias;
    private String tipoSanguineo;
    @Convert(converter = DadoSensivelConverter.class)
    private String necessidadesEspeciais;
    private String hospitalEmergencia;
    private String fotoUrl;
    @Enumerated(EnumType.STRING)
    private StatusAluno status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aluno_autorizados", joinColumns = @JoinColumn(name = "aluno_id"))
    @OrderColumn(name = "ordem")
    private List<AutorizadoEmbeddable> autorizados = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aluno_atividades", joinColumns = @JoinColumn(name = "aluno_id"))
    @Column(name = "atividade")
    @Enumerated(EnumType.STRING)
    private Set<AtividadeExtracurricular> atividades = new HashSet<>();

    protected AlunoEntity() {
    }

    static AlunoEntity de(Aluno a) {
        AlunoEntity e = new AlunoEntity();
        e.id = a.id();
        e.nome = a.nome();
        e.dataNascimento = a.dataNascimento();
        e.responsavelId = a.responsavelId();
        e.cpfResponsavel = a.cpfResponsavel().numero();
        e.alergias = a.informacoesMedicas().alergias();
        e.tipoSanguineo = a.informacoesMedicas().tipoSanguineo();
        e.necessidadesEspeciais = a.informacoesMedicas().necessidadesEspeciais();
        e.hospitalEmergencia = a.hospitalEmergencia();
        e.fotoUrl = a.fotoUrl();
        e.status = a.status();
        e.autorizados = new ArrayList<>(a.autorizados().stream().map(AutorizadoEmbeddable::de).toList());
        e.atividades = new HashSet<>(a.atividades());
        return e;
    }

    Aluno paraDominio() {
        return new Aluno(id, nome, dataNascimento, responsavelId, Cpf.de(cpfResponsavel),
                new InformacoesMedicas(alergias, tipoSanguineo, necessidadesEspeciais), hospitalEmergencia,
                autorizados.stream().map(AutorizadoEmbeddable::paraDominio).toList(), fotoUrl, status,
                atividades.isEmpty() ? Set.of() : EnumSet.copyOf(atividades));
    }
}
