import type { ArquivoBaixado, Serie, UUID } from "../models/comum";
import type { ItemMaterial, ListaMateriais } from "../models/material";

export interface MaterialGateway {
  listar(serie?: Serie): Promise<ListaMateriais[]>;
  publicada(serie: Serie): Promise<ListaMateriais>;
  baixarPdf(serie: Serie): Promise<ArquivoBaixado>;
  criar(
    serie: Serie,
    anoLetivo: number,
    itens: ItemMaterial[],
  ): Promise<ListaMateriais>;
  atualizarItens(id: UUID, itens: ItemMaterial[]): Promise<ListaMateriais>;
  publicar(
    id: UUID,
    vigenciaInicio: string,
    vigenciaFim: string,
  ): Promise<ListaMateriais>;
}
