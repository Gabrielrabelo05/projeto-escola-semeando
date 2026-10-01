import type { UUID } from "../models/comum";
import type { Contrato, FiltroContratos } from "../models/contrato";

export interface ContratoGateway {
  pesquisar(filtro: FiltroContratos): Promise<Contrato[]>;
  buscar(id: UUID): Promise<Contrato>;
  versoes(id: UUID): Promise<Contrato[]>;
  gerar(
    matriculaId: UUID,
    valorParcela: number,
    numeroParcelas: number,
  ): Promise<Contrato>;
  gerarNovaVersao(id: UUID): Promise<Contrato>;
  assinar(id: UUID): Promise<Contrato>;
}
