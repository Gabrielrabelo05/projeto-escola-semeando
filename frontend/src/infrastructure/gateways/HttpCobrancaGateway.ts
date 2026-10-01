import type { CobrancaGateway } from "@/domain/gateways";
import type {
  Cobranca,
  Inadimplente,
  PagamentoManual,
  RelatorioFinanceiro,
  ResultadoGeracao,
} from "@/domain/models/cobranca";
import type { UUID } from "@/domain/models/comum";
import type { HttpClient } from "../http/HttpClient";

export class HttpCobrancaGateway implements CobrancaGateway {
  constructor(private readonly http: HttpClient) {}

  listar(mes: string): Promise<Cobranca[]> {
    return this.http.get("/cobrancas", { mes });
  }

  doAluno(alunoId: UUID): Promise<Cobranca[]> {
    return this.http.get(`/cobrancas/aluno/${alunoId}`);
  }

  gerarMensal(mes: string): Promise<ResultadoGeracao> {
    return this.http.post("/cobrancas/gerar-mensal", { mes });
  }

  registrarPagamento(id: UUID, pagamento: PagamentoManual): Promise<Cobranca> {
    return this.http.post(`/cobrancas/${id}/pagamento-manual`, pagamento);
  }

  inadimplencia(mes?: string): Promise<Inadimplente[]> {
    return this.http.get("/cobrancas/inadimplencia", { mes });
  }

  relatorio(mes: string): Promise<RelatorioFinanceiro> {
    return this.http.get("/cobrancas/relatorio", { mes });
  }

  painel(mes: string): Promise<Record<string, number>> {
    return this.http.get("/cobrancas/painel", { mes });
  }
}
