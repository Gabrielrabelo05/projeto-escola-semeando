import type { NotificacaoGateway, PortalGateway } from "@/domain/gateways";
import type { Cobranca } from "@/domain/models/cobranca";
import type { UUID } from "@/domain/models/comum";
import type { Contrato } from "@/domain/models/contrato";
import type { Filho, Notificacao } from "@/domain/models/portal";
import type { HttpClient } from "../http/HttpClient";

export class HttpPortalGateway implements PortalGateway {
  constructor(private readonly http: HttpClient) {}

  filhos(): Promise<Filho[]> {
    return this.http.get("/portal/filhos");
  }

  boletos(alunoId: UUID): Promise<Cobranca[]> {
    return this.http.get(`/portal/filhos/${alunoId}/boletos`);
  }

  documentos(alunoId: UUID): Promise<Contrato[]> {
    return this.http.get(`/portal/filhos/${alunoId}/documentos`);
  }
}

export class HttpNotificacaoGateway implements NotificacaoGateway {
  constructor(private readonly http: HttpClient) {}

  minhas(somenteNaoLidas = false): Promise<Notificacao[]> {
    return this.http.get("/notificacoes", { naoLidas: somenteNaoLidas });
  }

  marcarComoLida(id: UUID): Promise<Notificacao> {
    return this.http.post(`/notificacoes/${id}/lida`);
  }
}
