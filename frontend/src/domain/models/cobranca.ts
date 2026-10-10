import type { AlunoResumo, DataISO, UUID } from "./comum";

/** Taxa de associado (Infantil) é paga na secretaria, sem boleto. */
export type TipoCobranca = "MENSALIDADE" | "TAXA_ASSOCIADO" | "EXTRACURRICULAR";
export type FormaPagamento =
  | "BOLETO"
  | "PIX"
  | "DINHEIRO"
  | "CARTAO"
  | "TRANSFERENCIA";

export interface Cobranca {
  id: UUID;
  aluno: AlunoResumo | null;
  tipo: TipoCobranca;
  mesReferencia: string;
  valor: number;
  /** Valor para pagamento até o vencimento (desconto de 10% nas mensalidades). */
  valorComDesconto: number;
  vencimento: DataISO;
  status: "EMITIDA" | "PAGA" | "CANCELADA";
  vencida: boolean;
  idBoletoCora: string | null;
  linhaDigitavel: string | null;
  urlBoleto: string | null;
  pagoEm: DataISO | null;
  valorPago: number | null;
  formaPagamento: FormaPagamento | null;
  /** Valor a pagar hoje: com desconto até o vencimento; depois, integral mais encargos. */
  valorDevido: number;
}

export interface Inadimplente {
  cobrancaId: UUID;
  aluno: AlunoResumo;
  mesReferencia: string;
  vencimento: DataISO;
  diasAtraso: number;
  valorOriginal: number;
  multa: number;
  juros: number;
  /** Correção pelo INPC (atraso de 30 dias ou mais). */
  correcao: number;
  valorAtualizado: number;
}

export interface RelatorioFinanceiro {
  mes: string;
  previsto: number;
  recebido: number;
  inadimplente: number;
  projecaoAnual: number;
  serieAnual: { mes: string; previsto: number; recebido: number }[];
}

export interface ResultadoGeracao {
  mes: string;
  geradas: string[];
  ignoradas: string[];
}

export interface PagamentoManual {
  pagoEm: DataISO;
  valorPago: number;
  forma: FormaPagamento;
}
