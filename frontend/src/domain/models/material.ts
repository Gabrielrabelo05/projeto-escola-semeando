import type { DataISO, Serie, UUID } from "./comum";

export type TipoItem = "MATERIAL" | "LIVRO";

export interface ItemMaterial {
  descricao: string;
  quantidade: number;
  tipo: TipoItem;
  linkCompra?: string | null;
}

export interface ListaMateriais {
  id: UUID;
  serie: Serie;
  serieDescricao: string;
  anoLetivo: number;
  status: "RASCUNHO" | "PUBLICADA" | "ARQUIVADA";
  vigenciaInicio: DataISO | null;
  vigenciaFim: DataISO | null;
  publicadaEm: DataISO | null;
  itens: ItemMaterial[];
}
