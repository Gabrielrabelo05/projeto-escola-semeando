import type { AlunoResumo, DataISO, UUID } from "./comum";

export type TipoCobranca = "TAXA_MATRICULA" | "MENSALIDADE" | "TAXA_ASSOCIADO";
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
  vencimento: DataISO;
  status: "EMITIDA" | "PAGA" | "CANCELADA";
  vencida: boolean;
  idBoletoCora: string | null;
  linhaDigitavel: string | null;
  urlBoleto: string | null;
  pagoEm: DataISO | null;
  valorPago: number | null;
  formaPagamento: FormaPagamento | null;
  valorAtualizado: number;
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
