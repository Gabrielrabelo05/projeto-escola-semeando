import type {
  Cobranca,
  Inadimplente,
  PagamentoManual,
  RelatorioFinanceiro,
  ResultadoGeracao,
} from "../models/cobranca";
import type { UUID } from "../models/comum";

export interface CobrancaGateway {
  listar(mes: string): Promise<Cobranca[]>;
  doAluno(alunoId: UUID): Promise<Cobranca[]>;
  gerarMensal(mes: string): Promise<ResultadoGeracao>;
  registrarPagamento(id: UUID, pagamento: PagamentoManual): Promise<Cobranca>;
  inadimplencia(mes?: string): Promise<Inadimplente[]>;
  relatorio(mes: string): Promise<RelatorioFinanceiro>;
  painel(mes: string): Promise<Record<string, number>>;
}
