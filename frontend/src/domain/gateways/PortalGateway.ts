import type { Cobranca } from "../models/cobranca";
import type { UUID } from "../models/comum";
import type { Contrato } from "../models/contrato";
import type { Filho, Notificacao } from "../models/portal";

export interface PortalGateway {
  filhos(): Promise<Filho[]>;
  boletos(alunoId: UUID): Promise<Cobranca[]>;
  documentos(alunoId: UUID): Promise<Contrato[]>;
}

export interface NotificacaoGateway {
  minhas(somenteNaoLidas?: boolean): Promise<Notificacao[]>;
  marcarComoLida(id: UUID): Promise<Notificacao>;
}
