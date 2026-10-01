import type { ObrigacaoGateway } from "@/domain/gateways";
import type { ArquivoBaixado, UUID } from "@/domain/models/comum";
import type {
  NovaObrigacao,
  Obrigacao,
  StatusObrigacao,
} from "@/domain/models/obrigacao";
import type { HttpClient } from "../http/HttpClient";

export class HttpObrigacaoGateway implements ObrigacaoGateway {
  constructor(private readonly http: HttpClient) {}

  listar(filtro: {
    ano?: number;
    status?: StatusObrigacao;
  }): Promise<Obrigacao[]> {
    return this.http.get("/obrigacoes", filtro);
  }

  criar(dados: NovaObrigacao): Promise<Obrigacao> {
    return this.http.post("/obrigacoes", dados);
  }

  gerarCalendarioAnual(ano: number, responsavel: string): Promise<Obrigacao[]> {
    return this.http.post("/obrigacoes/calendario-anual", { ano, responsavel });
  }

  marcarItem(id: UUID, indice: number, concluido: boolean): Promise<Obrigacao> {
    return this.http.put(`/obrigacoes/${id}/checklist/${indice}`, undefined, {
      params: { concluido },
    });
  }

  anexarComprovante(id: UUID, arquivo: File): Promise<Obrigacao> {
    return this.http.upload(
      `/obrigacoes/${id}/comprovantes`,
      "arquivo",
      arquivo,
    );
  }

  baixarComprovante(id: UUID, indice: number): Promise<ArquivoBaixado> {
    return this.http.download(`/obrigacoes/${id}/comprovantes/${indice}`);
  }

  concluir(id: UUID): Promise<Obrigacao> {
    return this.http.post(`/obrigacoes/${id}/concluir`);
  }

  verificarAlertas(): Promise<number> {
    return this.http.post("/obrigacoes/verificar-alertas");
  }

  exportarCenso(ano: number): Promise<ArquivoBaixado> {
    return this.http.download("/obrigacoes/exportacoes/censo", { ano });
  }

  exportarSistemaPresenca(
    ano: number,
    bimestre: number,
  ): Promise<ArquivoBaixado> {
    return this.http.download("/obrigacoes/exportacoes/sistema-presenca", {
      ano,
      bimestre,
    });
  }
}
