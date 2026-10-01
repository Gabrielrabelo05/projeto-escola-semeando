package br.org.semeando.digital.domain.contrato;

import br.org.semeando.digital.domain.shared.DomainException;
import br.org.semeando.digital.domain.shared.RegraNegocioVioladaException;
import br.org.semeando.digital.domain.shared.Validacoes;
import br.org.semeando.digital.domain.turma.NivelEnsino;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Vínculo formal entre a família e a escola. A hierarquia é selada: só existem a Ficha de Sócio
 * (Educação Infantil) e o Contrato de Prestação de Serviços (Ensino Fundamental) – RN-01.
 */
public abstract sealed class DocumentoVinculo permits FichaSocio, ContratoPrestacaoServicos {

    private final UUID id;
    private final UUID numero;
    private final int versao;
    private final UUID alunoId;
    private final UUID matriculaId;
    private final int anoLetivo;
    private final Instant geradoEm;
    private final String geradoPor;
    private final String conteudo;
    private final String hashDocumento;
    private StatusDocumentoVinculo status;
    private AssinaturaEletronica assinatura;

    protected DocumentoVinculo(DadosVinculo dados) {
        this.id = Validacoes.obrigatorio(dados.id(), "id");
        this.numero = Validacoes.obrigatorio(dados.numero(), "número");
        this.versao = Validacoes.positivo(dados.versao(), "versão");
        this.alunoId = Validacoes.obrigatorio(dados.alunoId(), "aluno");
        this.matriculaId = Validacoes.obrigatorio(dados.matriculaId(), "matrícula");
        this.anoLetivo = dados.anoLetivo();
        this.geradoEm = Validacoes.obrigatorio(dados.geradoEm(), "data de geração");
        this.geradoPor = Validacoes.obrigatorio(dados.geradoPor(), "usuário que gerou");
        this.conteudo = Validacoes.obrigatorio(dados.conteudo(), "conteúdo");
        this.hashDocumento = dados.hashDocumento() == null ? sha256(conteudo) : dados.hashDocumento();
        this.status = Validacoes.obrigatorio(dados.status(), "status");
        this.assinatura = dados.assinatura();
    }

    public abstract TipoVinculo tipo();

    /** RN-01: o tipo do vínculo precisa corresponder ao nível de ensino do aluno. */
    public void validarCompatibilidade(NivelEnsino nivel) {
        if (tipo().nivel() != nivel) {
            throw new RegraNegocioVioladaException("RN-01",
                    tipo() + " não pode ser utilizado para " + nivel.descricao());
        }
    }

    /** RF-15: registra a assinatura eletrônica com data, IP e hash do documento. */
    public void assinar(String assinante, String enderecoIp, Instant agora) {
        if (status != StatusDocumentoVinculo.AGUARDANDO_ASSINATURA) {
            throw new DomainException("STATUS_INVALIDO", "Documento não está aguardando assinatura");
        }
        this.assinatura = new AssinaturaEletronica(assinante, agora, enderecoIp, hashDocumento);
        this.status = StatusDocumentoVinculo.ASSINADO;
    }

    /** RF-16: qualquer alteração gera uma nova versão; a anterior fica marcada como substituída. */
    public void marcarComoSubstituido() {
        this.status = StatusDocumentoVinculo.SUBSTITUIDO;
    }

    public boolean assinado() {
        return status == StatusDocumentoVinculo.ASSINADO;
    }

    protected DadosVinculo dadosProximaVersao(String conteudo, String geradoPor, Instant agora) {
        return new DadosVinculo(UUID.randomUUID(), numero, versao + 1, alunoId, matriculaId, anoLetivo, agora,
                geradoPor, conteudo, null, StatusDocumentoVinculo.AGUARDANDO_ASSINATURA, null);
    }

    public static String sha256(String conteudo) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public UUID id() {
        return id;
    }

    public UUID numero() {
        return numero;
    }

    public int versao() {
        return versao;
    }

    public UUID alunoId() {
        return alunoId;
    }

    public UUID matriculaId() {
        return matriculaId;
    }

    public int anoLetivo() {
        return anoLetivo;
    }

    public Instant geradoEm() {
        return geradoEm;
    }

    public String geradoPor() {
        return geradoPor;
    }

    public String conteudo() {
        return conteudo;
    }

    public String hashDocumento() {
        return hashDocumento;
    }

    public StatusDocumentoVinculo status() {
        return status;
    }

    public AssinaturaEletronica assinatura() {
        return assinatura;
    }
}
