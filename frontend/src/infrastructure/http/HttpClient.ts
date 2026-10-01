import type { ArquivoBaixado } from "@/domain/models/comum";

export type Parametros = Record<
  string,
  string | number | boolean | null | undefined
>;

export interface OpcoesRequisicao {
  params?: Parametros;
  headers?: Record<string, string>;
}

/** Contrato de transporte HTTP usado pelos gateways – esconde a biblioteca concreta (axios). */
export interface HttpClient {
  get<T>(url: string, params?: Parametros): Promise<T>;
  post<T>(url: string, corpo?: unknown, opcoes?: OpcoesRequisicao): Promise<T>;
  put<T>(url: string, corpo?: unknown, opcoes?: OpcoesRequisicao): Promise<T>;
  patch<T>(url: string, corpo?: unknown, opcoes?: OpcoesRequisicao): Promise<T>;
  delete<T = void>(url: string): Promise<T>;
  upload<T>(url: string, campo: string, arquivo: File): Promise<T>;
  download(url: string, params?: Parametros): Promise<ArquivoBaixado>;
}
