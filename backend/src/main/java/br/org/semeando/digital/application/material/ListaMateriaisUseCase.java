package br.org.semeando.digital.application.material;

import br.org.semeando.digital.application.auditoria.Auditor;
import br.org.semeando.digital.application.port.GeradorPdf;
import br.org.semeando.digital.application.shared.Ator;
import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.application.shared.Notificador;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.auditoria.AcaoAuditada;
import br.org.semeando.digital.domain.material.ItemMaterial;
import br.org.semeando.digital.domain.material.ListaMateriais;
import br.org.semeando.digital.domain.material.ListaMateriaisRepository;
import br.org.semeando.digital.domain.material.TipoItem;
import br.org.semeando.digital.domain.matricula.MatriculaRepository;
import br.org.semeando.digital.domain.pessoa.ResponsavelRepository;
import br.org.semeando.digital.domain.shared.IdentidadeInstitucional;
import br.org.semeando.digital.domain.shared.RecursoNaoEncontradoException;
import br.org.semeando.digital.domain.turma.Serie;
import br.org.semeando.digital.domain.turma.TurmaRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Módulo 3 – RF-05, RF-19 a RF-22. */
@CasoDeUso
public class ListaMateriaisUseCase {

    private final ListaMateriaisRepository listas;
    private final TurmaRepository turmas;
    private final MatriculaRepository matriculas;
    private final AlunoRepository alunos;
    private final ResponsavelRepository responsaveis;
    private final Notificador notificador;
    private final GeradorPdf pdf;
    private final Auditor auditor;
    private final Clock clock;

    public ListaMateriaisUseCase(ListaMateriaisRepository listas, TurmaRepository turmas,
            MatriculaRepository matriculas, AlunoRepository alunos, ResponsavelRepository responsaveis,
            Notificador notificador, GeradorPdf pdf, Auditor auditor, Clock clock) {
        this.listas = listas;
        this.turmas = turmas;
        this.matriculas = matriculas;
        this.alunos = alunos;
        this.responsaveis = responsaveis;
        this.notificador = notificador;
        this.pdf = pdf;
        this.auditor = auditor;
        this.clock = clock;
    }

    public List<ListaMateriais> listar(Serie serie) {
        return listas.listar(serie);
    }

    public ListaMateriais buscar(UUID id) {
        return listas.buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException("Lista", id));
    }

    public ListaMateriais publicada(Serie serie) {
        return listas.buscarPublicada(serie)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lista publicada", serie));
    }

    public ListaMateriais criar(Serie serie, int anoLetivo, List<ItemMaterial> itens, Ator ator) {
        ListaMateriais lista = listas.salvar(ListaMateriais.rascunho(serie, anoLetivo, itens));
        auditor.registrar(ator, AcaoAuditada.CRIACAO, "ListaMateriais", lista.id(), serie.descricao());
        return lista;
    }

    public ListaMateriais atualizarItens(UUID id, List<ItemMaterial> itens, Ator ator) {
        ListaMateriais lista = buscar(id);
        lista.atualizarItens(itens);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "ListaMateriais", id, itens.size() + " itens");
        return listas.salvar(lista);
    }

    /** RF-20 / RF-21: publica, arquiva a anterior e notifica os responsáveis da série. */
    public ListaMateriais publicar(UUID id, LocalDate inicio, LocalDate fim, Ator ator) {
        ListaMateriais lista = buscar(id);
        listas.buscarPublicada(lista.serie()).ifPresent(anterior -> {
            anterior.arquivar();
            listas.salvar(anterior);
        });
        lista.publicar(inicio, fim, LocalDate.now(clock));
        listas.salvar(lista);
        auditor.registrar(ator, AcaoAuditada.EDICAO, "ListaMateriais", id, "Publicada");
        notificarResponsaveis(lista);
        return lista;
    }

    /** RF-22: exportação em PDF. */
    public byte[] exportarPdf(Serie serie) {
        ListaMateriais lista = publicada(serie);
        List<String> linhas = new ArrayList<>();
        linhas.add(IdentidadeInstitucional.cabecalho());
        linhas.add("Vigência: " + lista.vigenciaInicio() + " a " + lista.vigenciaFim());
        linhas.add("MATERIAIS");
        lista.itens().stream().filter(i -> i.tipo() == TipoItem.MATERIAL)
                .forEach(i -> linhas.add("• " + i.quantidade() + "x " + i.descricao()));
        linhas.add("LIVROS");
        lista.livros().forEach(i -> linhas.add("• " + i.quantidade() + "x " + i.descricao()
                + (i.linkCompra() == null ? "" : " – " + i.linkCompra())));
        return pdf.gerar("Lista de materiais – " + serie.descricao() + " – " + lista.anoLetivo(), linhas);
    }

    private void notificarResponsaveis(ListaMateriais lista) {
        turmas.listarPorAnoLetivo(lista.anoLetivo()).stream()
                .filter(turma -> turma.serie() == lista.serie())
                .flatMap(turma -> matriculas.alunosAtivosDaTurma(turma.id()).stream())
                .flatMap(alunoId -> alunos.buscarPorId(alunoId).stream())
                .flatMap(aluno -> responsaveis.buscarPorId(aluno.responsavelId()).stream())
                .distinct()
                .forEach(responsavel -> notificador.responsavel(responsavel, "Nova lista de materiais",
                        "A lista de materiais do " + lista.serie().descricao() + " foi publicada no portal."));
    }
}
