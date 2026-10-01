package br.org.semeando.digital.application.cobranca;

import br.org.semeando.digital.application.shared.CasoDeUso;
import br.org.semeando.digital.domain.aluno.Aluno;
import br.org.semeando.digital.domain.aluno.AlunoRepository;
import br.org.semeando.digital.domain.cobranca.Cobranca;
import br.org.semeando.digital.domain.cobranca.CobrancaRepository;
import br.org.semeando.digital.domain.cobranca.Encargos;
import br.org.semeando.digital.domain.cobranca.StatusCobranca;
import br.org.semeando.digital.domain.shared.Dinheiro;
import br.org.semeando.digital.domain.shared.MesReferencia;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** RF-26 (inadimplência), RF-29 (relatório mensal) e RF-51 (painel de status dos boletos). */
@CasoDeUso
public class ConsultarFinanceiroUseCase {

    private final CobrancaRepository cobrancas;
    private final AlunoRepository alunos;
    private final Clock clock;

    public ConsultarFinanceiroUseCase(CobrancaRepository cobrancas, AlunoRepository alunos, Clock clock) {
        this.cobrancas = cobrancas;
        this.alunos = alunos;
        this.clock = clock;
    }

    public List<CobrancaComAluno> listar(MesReferencia mes) {
        return comAlunos(cobrancas.listarPorMes(mes));
    }

    public List<CobrancaComAluno> doAluno(UUID alunoId) {
        return comAlunos(cobrancas.listarPorAluno(alunoId));
    }

    /** RF-26: alunos inadimplentes com valor original, juros e multa calculados. */
    public List<Inadimplente> inadimplencia(MesReferencia mes) {
        LocalDate hoje = LocalDate.now(clock);
        List<Cobranca> base = mes == null ? cobrancas.listarEmAberto() : cobrancas.listarPorMes(mes);
        return comAlunos(base.stream().filter(c -> c.vencidaEm(hoje)).toList()).stream()
                .map(item -> new Inadimplente(item.cobranca(), item.aluno(), item.cobranca().encargosEm(hoje),
                        item.cobranca().valorAtualizadoEm(hoje)))
                .sorted(Comparator.comparing((Inadimplente i) -> i.cobranca().vencimento()))
                .toList();
    }

    /** RF-29: total previsto, recebido, inadimplência e projeção anual. */
    public RelatorioFinanceiro relatorioMensal(MesReferencia mes) {
        LocalDate hoje = LocalDate.now(clock);
        List<Cobranca> doMes = ativas(cobrancas.listarPorMes(mes));
        Dinheiro previsto = somar(doMes);
        Dinheiro recebido = doMes.stream().filter(c -> c.status() == StatusCobranca.PAGA)
                .map(c -> c.pagamento().valorPago()).reduce(Dinheiro.ZERO, Dinheiro::somar);
        Dinheiro inadimplente = somar(doMes.stream().filter(c -> c.vencidaEm(hoje)).toList());

        List<Cobranca> doAno = ativas(cobrancas.listarPorAno(mes.valor().getYear()));
        Dinheiro projecao = projecaoAnual(doAno, mes);
        List<ResumoMensal> serie = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(mes.valor().getYear(), m);
            List<Cobranca> c = doAno.stream().filter(x -> x.mesReferencia().valor().equals(ym)).toList();
            serie.add(new ResumoMensal(ym.toString(), somar(c),
                    somar(c.stream().filter(x -> x.status() == StatusCobranca.PAGA).toList())));
        }
        return new RelatorioFinanceiro(mes, previsto, recebido, inadimplente, projecao, serie);
    }

    /** RF-51: quantidade de boletos por status (vencido é derivado da data). */
    public Map<String, Long> painelBoletos(MesReferencia mes) {
        LocalDate hoje = LocalDate.now(clock);
        return cobrancas.listarPorMes(mes).stream()
                .collect(Collectors.groupingBy(c -> c.vencidaEm(hoje) ? "VENCIDA" : c.status().name(),
                        Collectors.counting()));
    }

    /** Recebido até agora + previsto para o restante do ano (base: média dos meses já emitidos). */
    private Dinheiro projecaoAnual(List<Cobranca> doAno, MesReferencia mes) {
        long mesesEmitidos = doAno.stream().map(Cobranca::mesReferencia).distinct().count();
        if (mesesEmitidos == 0) {
            return Dinheiro.ZERO;
        }
        Dinheiro emitido = somar(doAno);
        Dinheiro media = Dinheiro.de(emitido.valor().divide(BigDecimal.valueOf(mesesEmitidos), 2,
                RoundingMode.HALF_UP));
        long mesesRestantes = Math.max(0, 12 - mes.valor().getMonthValue());
        return emitido.somar(media.multiplicar(mesesRestantes));
    }

    private static List<Cobranca> ativas(List<Cobranca> cobrancas) {
        return cobrancas.stream().filter(c -> c.status() != StatusCobranca.CANCELADA).toList();
    }

    private static Dinheiro somar(List<Cobranca> cobrancas) {
        return cobrancas.stream().map(Cobranca::valor).reduce(Dinheiro.ZERO, Dinheiro::somar);
    }

    private List<CobrancaComAluno> comAlunos(List<Cobranca> lista) {
        Map<UUID, Aluno> cache = lista.stream().map(Cobranca::alunoId).distinct()
                .map(alunos::buscarPorId).flatMap(Optional::stream)
                .collect(Collectors.toMap(Aluno::id, Function.identity()));
        return lista.stream().map(c -> new CobrancaComAluno(c, cache.get(c.alunoId()))).toList();
    }

    public record CobrancaComAluno(Cobranca cobranca, Aluno aluno) {
    }

    public record Inadimplente(Cobranca cobranca, Aluno aluno, Encargos encargos, Dinheiro valorAtualizado) {
    }

    public record ResumoMensal(String mes, Dinheiro previsto, Dinheiro recebido) {
    }

    public record RelatorioFinanceiro(MesReferencia mes, Dinheiro previsto, Dinheiro recebido, Dinheiro inadimplente,
            Dinheiro projecaoAnual, List<ResumoMensal> serieAnual) {
    }
}
