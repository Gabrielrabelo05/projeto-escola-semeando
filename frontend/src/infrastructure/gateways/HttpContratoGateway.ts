import type { ContratoGateway } from "@/domain/gateways";
import type { UUID } from "@/domain/models/comum";
import type { Contrato, FiltroContratos } from "@/domain/models/contrato";
import type { HttpClient } from "../http/HttpClient";

export class HttpContratoGateway implements ContratoGateway {
  constructor(private readonly http: HttpClient) {}

  pesquisar(filtro: FiltroContratos): Promise<Contrato[]> {
    return this.http.get("/contratos", { ...filtro });
  }

  buscar(id: UUID): Promise<Contrato> {
    return this.http.get(`/contratos/${id}`);
  }

  versoes(id: UUID): Promise<Contrato[]> {
    return this.http.get(`/contratos/${id}/versoes`);
  }

  gerar(
    matriculaId: UUID,
    valorParcela: number,
    numeroParcelas: number,
  ): Promise<Contrato> {
    return this.http.post("/contratos", {
      matriculaId,
      valorParcela,
      numeroParcelas,
    });
  }

  gerarNovaVersao(id: UUID): Promise<Contrato> {
    return this.http.post(`/contratos/${id}/nova-versao`);
  }

  assinar(id: UUID): Promise<Contrato> {
    return this.http.post(`/contratos/${id}/assinar`);
  }
}
