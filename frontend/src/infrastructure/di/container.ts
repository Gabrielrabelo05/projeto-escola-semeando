import type { Gateways } from "@/domain/gateways";
import { HttpAuthGateway } from "../gateways/HttpAuthGateway";
import { HttpCadastroGateway } from "../gateways/HttpCadastroGateway";
import { HttpCobrancaGateway } from "../gateways/HttpCobrancaGateway";
import { HttpContratoGateway } from "../gateways/HttpContratoGateway";
import { HttpMaterialGateway } from "../gateways/HttpMaterialGateway";
import { HttpMatriculaGateway } from "../gateways/HttpMatriculaGateway";
import { HttpObrigacaoGateway } from "../gateways/HttpObrigacaoGateway";
import { HttpPedagogicoGateway } from "../gateways/HttpPedagogicoGateway";
import {
  HttpNotificacaoGateway,
  HttpPortalGateway,
} from "../gateways/HttpPortalGateway";
import type { HttpClient } from "../http/HttpClient";

/** Composition root: injeta o mesmo HttpClient em todos os gateways HTTP. */
export function criarGateways(http: HttpClient): Gateways {
  return {
    auth: new HttpAuthGateway(http),
    matricula: new HttpMatriculaGateway(http),
    contrato: new HttpContratoGateway(http),
    cobranca: new HttpCobrancaGateway(http),
    cadastro: new HttpCadastroGateway(http),
    pedagogico: new HttpPedagogicoGateway(http),
    obrigacao: new HttpObrigacaoGateway(http),
    material: new HttpMaterialGateway(http),
    portal: new HttpPortalGateway(http),
    notificacao: new HttpNotificacaoGateway(http),
  };
}

export const API_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api";
