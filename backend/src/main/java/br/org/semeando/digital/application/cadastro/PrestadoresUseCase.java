package br.org.semeando.digital.application.cadastro;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.pessoa.Contato;
import br.org.semeando.digital.domain.pessoa.PrestadorServico;
import br.org.semeando.digital.domain.pessoa.PrestadorServicoRepository;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** RF-34 */
@CasoDeUso
public class PrestadoresUseCase {

    private final PrestadorServicoRepository prestadores;
    private final Auditor auditor;

    public PrestadoresUseCase(PrestadorServicoRepository prestadores, Auditor auditor) {
        this.prestadores = prestadores;
        this.auditor = auditor;
    }

    public List<PrestadorServico> listar(boolean incluirInativos) {
        return prestadores.listar(incluirInativos);
    }

    public PrestadorServico buscar(UUID id) {
        return prestadores.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Prestador", id));
    }

    public PrestadorServico cadastrar(DadosPrestador dados, Ator ator) {
        PrestadorServico prestador = prestadores.salvar(PrestadorServico.novo(dados.nome(), dados.documento(),
                dados.servicoPrestado(), dados.vigenciaInicio(), dados.vigenciaFim(), dados.documentosObrigatorios(),
                dados.contato()));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "Prestador", prestador.id(), prestador.nome());
        return prestador;
    }

    public PrestadorServico atualizar(UUID id, DadosPrestador dados, Ator ator) {
        PrestadorServico prestador = buscar(id);
        prestador.atualizar(dados.nome(), dados.servicoPrestado(), dados.vigenciaInicio(), dados.vigenciaFim(),
                dados.documentosObrigatorios(), dados.contato());
        auditor.registrar(ator, AcaoAuditada.EDICAO, "Prestador", id, prestador.nome());
        return prestadores.salvar(prestador);
    }

    public PrestadorServico inativar(UUID id, Ator ator) {
        PrestadorServico prestador = buscar(id);
        prestador.inativar();
        auditor.registrar(ator, AcaoAuditada.INATIVACAO, "Prestador", id, prestador.nome());
        return prestadores.salvar(prestador);
    }

    public record DadosPrestador(String nome, String documento, String servicoPrestado, LocalDate vigenciaInicio,
            LocalDate vigenciaFim, List<String> documentosObrigatorios, Contato contato) {
    }
}
