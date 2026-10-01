import type { MaterialGateway } from "@/domain/gateways";
import type { ArquivoBaixado, Serie, UUID } from "@/domain/models/comum";
import type { ItemMaterial, ListaMateriais } from "@/domain/models/material";
import type { HttpClient } from "../http/HttpClient";

export class HttpMaterialGateway implements MaterialGateway {
  constructor(private readonly http: HttpClient) {}

  listar(serie?: Serie): Promise<ListaMateriais[]> {
    return this.http.get("/listas-materiais", { serie });
  }

  publicada(serie: Serie): Promise<ListaMateriais> {
    return this.http.get(`/listas-materiais/publicada/${serie}`);
  }

  baixarPdf(serie: Serie): Promise<ArquivoBaixado> {
    return this.http.download(`/listas-materiais/publicada/${serie}/pdf`);
  }

  criar(
    serie: Serie,
    anoLetivo: number,
    itens: ItemMaterial[],
  ): Promise<ListaMateriais> {
    return this.http.post("/listas-materiais", { serie, anoLetivo, itens });
  }

  atualizarItens(id: UUID, itens: ItemMaterial[]): Promise<ListaMateriais> {
    return this.http.put(`/listas-materiais/${id}/itens`, itens);
  }

  publicar(
    id: UUID,
    vigenciaInicio: string,
    vigenciaFim: string,
  ): Promise<ListaMateriais> {
    return this.http.post(`/listas-materiais/${id}/publicar`, {
      vigenciaInicio,
      vigenciaFim,
    });
  }
}
