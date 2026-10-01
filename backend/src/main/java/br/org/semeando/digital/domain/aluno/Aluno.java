package br.org.semeando.digital.domain.aluno;

import br.org.semeando.digital.domain.shared.Cpf;
import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-07 / RF-31: ficha completa do aluno, vinculada ao CPF do responsável. */
public class Aluno {

    private final UUID id;
    private String nome;
    private LocalDate dataNascimento;
    private final UUID responsavelId;
    private final Cpf cpfResponsavel;
    private InformacoesMedicas informacoesMedicas;
    private String hospitalEmergencia;
    private List<PessoaAutorizada> autorizados;
    private String fotoUrl;
    private StatusAluno status;

    public Aluno(UUID id, String nome, LocalDate dataNascimento, UUID responsavelId, Cpf cpfResponsavel,
            InformacoesMedicas informacoesMedicas, String hospitalEmergencia, List<PessoaAutorizada> autorizados,
            String fotoUrl, StatusAluno status) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.responsavelId = Validacoes.obrigatorio(responsavelId, "responsável");
        this.cpfResponsavel = Validacoes.obrigatorio(cpfResponsavel, "CPF do responsável");
        this.status = Validacoes.obrigatorio(status, "status");
        atualizarFicha(nome, dataNascimento, informacoesMedicas, hospitalEmergencia, autorizados, fotoUrl);
    }

    public static Aluno preMatricular(String nome, LocalDate dataNascimento, UUID responsavelId, Cpf cpfResponsavel,
            InformacoesMedicas informacoesMedicas, String hospitalEmergencia, List<PessoaAutorizada> autorizados) {
        return new Aluno(UUID.randomUUID(), nome, dataNascimento, responsavelId, cpfResponsavel, informacoesMedicas,
                hospitalEmergencia, autorizados, null, StatusAluno.PRE_MATRICULA);
    }

    public final void atualizarFicha(String nome, LocalDate dataNascimento, InformacoesMedicas informacoesMedicas,
            String hospitalEmergencia, List<PessoaAutorizada> autorizados, String fotoUrl) {
        this.nome = Validacoes.obrigatorio(nome, "nome do aluno");
        this.dataNascimento = Validacoes.obrigatorio(dataNascimento, "data de nascimento");
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new DomainException("DATA_INVALIDA", "Data de nascimento não pode estar no futuro");
        }
        this.informacoesMedicas = informacoesMedicas == null ? InformacoesMedicas.naoInformadas() : informacoesMedicas;
        this.hospitalEmergencia = hospitalEmergencia;
        this.autorizados = List.copyOf(autorizados == null ? List.of() : autorizados);
        this.fotoUrl = fotoUrl;
    }

    /** RN-06: hospital de emergência e autorizados a buscar são obrigatórios para o vínculo contratual. */
    public boolean possuiDadosDeSeguranca() {
        return hospitalEmergencia != null && !hospitalEmergencia.isBlank() && !autorizados.isEmpty();
    }

    public void ativar() {
        this.status = StatusAluno.ATIVO;
    }

    public void transferir() {
        this.status = StatusAluno.TRANSFERIDO;
    }

    public void cancelar() {
        this.status = StatusAluno.CANCELADO;
    }

    /** RF-37: o registro nunca é excluído, apenas inativado. */
    public void inativar() {
        this.status = StatusAluno.INATIVO;
    }

    public boolean ativo() {
        return status == StatusAluno.ATIVO;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public LocalDate dataNascimento() {
        return dataNascimento;
    }

    public UUID responsavelId() {
        return responsavelId;
    }

    public Cpf cpfResponsavel() {
        return cpfResponsavel;
    }

    public InformacoesMedicas informacoesMedicas() {
        return informacoesMedicas;
    }

    public String hospitalEmergencia() {
        return hospitalEmergencia;
    }

    public List<PessoaAutorizada> autorizados() {
        return autorizados;
    }

    public String fotoUrl() {
        return fotoUrl;
    }

    public StatusAluno status() {
        return status;
    }
}
