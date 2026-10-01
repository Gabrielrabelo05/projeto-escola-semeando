import type { Autorizado, DataISO, UUID } from "./comum";

export type TipoVinculo = "FICHA_SOCIO" | "CONTRATO_PRESTACAO_SERVICOS";
export type StatusContrato =
  | "AGUARDANDO_ASSINATURA"
  | "ASSINADO"
  | "SUBSTITUIDO";

export interface Assinatura {
  assinante: string;
  assinadoEm: DataISO;
  enderecoIp: string;
  hashDocumento: string;
}

export interface Contrato {
  id: UUID;
  numero: UUID;
  versao: number;
  tipo: TipoVinculo;
  alunoId: UUID;
  matriculaId: UUID;
  anoLetivo: number;
  geradoEm: DataISO;
  geradoPor: string;
  conteudo: string;
  hashDocumento: string;
  status: StatusContrato;
  assinatura: Assinatura | null;
  valorParcela: number | null;
  numeroParcelas: number | null;
  valorTotal: number | null;
  hospitalEmergencia: string | null;
  autorizados: Autorizado[];
  nomeSocio: string | null;
  valorTaxaAssociado: number | null;
}

export interface FiltroContratos {
  alunoId?: UUID;
  turmaId?: UUID;
  anoLetivo?: number;
  status?: StatusContrato;
}
