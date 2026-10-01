package br.org.semeando.digital.domain.pessoa;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.Validacoes;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-34: prestador de serviços (pessoa física ou jurídica) com vigência de contrato. */
public final class PrestadorServico {

    private final UUID id;
    private String nome;
    private final String documento;
    private String servicoPrestado;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFim;
    private List<String> documentosObrigatorios;
    private Contato contato;
    private boolean ativo;

    public PrestadorServico(UUID id, String nome, String documento, String servicoPrestado,
            LocalDate vigenciaInicio, LocalDate vigenciaFim, List<String> documentosObrigatorios, Contato contato,
            boolean ativo) {
        this.id = Validacoes.obrigatorio(id, "id");
        this.documento = Validacoes.obrigatorio(documento, "CPF/CNPJ").replaceAll("\\D", "");
        if (this.documento.length() != 11 && this.documento.length() != 14) {
            throw new DomainException("DOCUMENTO_INVALIDO", "Informe um CPF (11 dígitos) ou CNPJ (14 dígitos)");
        }
        this.contato = contato == null ? Contato.vazio() : contato;
        this.ativo = ativo;
        atualizar(nome, servicoPrestado, vigenciaInicio, vigenciaFim, documentosObrigatorios, this.contato);
    }

    public static PrestadorServico novo(String nome, String documento, String servicoPrestado,
            LocalDate vigenciaInicio, LocalDate vigenciaFim, List<String> documentosObrigatorios, Contato contato) {
        return new PrestadorServico(UUID.randomUUID(), nome, documento, servicoPrestado, vigenciaInicio,
                vigenciaFim, documentosObrigatorios, contato, true);
    }

    public void atualizar(String nome, String servicoPrestado, LocalDate vigenciaInicio, LocalDate vigenciaFim,
            List<String> documentosObrigatorios, Contato contato) {
        this.nome = Validacoes.obrigatorio(nome, "nome");
        this.servicoPrestado = Validacoes.obrigatorio(servicoPrestado, "serviço prestado");
        this.vigenciaInicio = Validacoes.obrigatorio(vigenciaInicio, "início da vigência");
        this.vigenciaFim = Validacoes.obrigatorio(vigenciaFim, "fim da vigência");
        if (vigenciaFim.isBefore(vigenciaInicio)) {
            throw new DomainException("VIGENCIA_INVALIDA", "Fim da vigência deve ser após o início");
        }
        this.documentosObrigatorios = List.copyOf(documentosObrigatorios == null ? List.of() : documentosObrigatorios);
        this.contato = contato == null ? Contato.vazio() : contato;
    }

    public boolean contratoVigente(LocalDate hoje) {
        return !hoje.isBefore(vigenciaInicio) && !hoje.isAfter(vigenciaFim);
    }

    public void inativar() {
        this.ativo = false;
    }

    public boolean pessoaJuridica() {
        return documento.length() == 14;
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public String documento() {
        return documento;
    }

    public String servicoPrestado() {
        return servicoPrestado;
    }

    public LocalDate vigenciaInicio() {
        return vigenciaInicio;
    }

    public LocalDate vigenciaFim() {
        return vigenciaFim;
    }

    public List<String> documentosObrigatorios() {
        return documentosObrigatorios;
    }

    public Contato contato() {
        return contato;
    }

    public boolean ativo() {
        return ativo;
    }
}
