import type { AuthGateway } from "./AuthGateway";
import type { CadastroGateway } from "./CadastroGateway";
import type { CobrancaGateway } from "./CobrancaGateway";
import type { ContratoGateway } from "./ContratoGateway";
import type { MaterialGateway } from "./MaterialGateway";
import type { MatriculaGateway } from "./MatriculaGateway";
import type { ObrigacaoGateway } from "./ObrigacaoGateway";
import type { PedagogicoGateway } from "./PedagogicoGateway";
import type { NotificacaoGateway, PortalGateway } from "./PortalGateway";

/** Conjunto de portas de saída consumidas pela camada de apresentação. */
export interface Gateways {
  auth: AuthGateway;
  matricula: MatriculaGateway;
  contrato: ContratoGateway;
  cobranca: CobrancaGateway;
  cadastro: CadastroGateway;
  pedagogico: PedagogicoGateway;
  obrigacao: ObrigacaoGateway;
  material: MaterialGateway;
  portal: PortalGateway;
  notificacao: NotificacaoGateway;
}

export type {
  AuthGateway,
  CadastroGateway,
  CobrancaGateway,
  ContratoGateway,
  MaterialGateway,
  MatriculaGateway,
  NotificacaoGateway,
  ObrigacaoGateway,
  PedagogicoGateway,
  PortalGateway,
};
