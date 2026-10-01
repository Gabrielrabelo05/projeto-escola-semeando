import type { ArquivoBaixado, UUID } from "../models/comum";
import type {
  NovaObrigacao,
  Obrigacao,
  StatusObrigacao,
} from "../models/obrigacao";

export interface ObrigacaoGateway {
  listar(filtro: {
    ano?: number;
    status?: StatusObrigacao;
  }): Promise<Obrigacao[]>;
  criar(dados: NovaObrigacao): Promise<Obrigacao>;
  gerarCalendarioAnual(ano: number, responsavel: string): Promise<Obrigacao[]>;
  marcarItem(id: UUID, indice: number, concluido: boolean): Promise<Obrigacao>;
  anexarComprovante(id: UUID, arquivo: File): Promise<Obrigacao>;
  baixarComprovante(id: UUID, indice: number): Promise<ArquivoBaixado>;
  concluir(id: UUID): Promise<Obrigacao>;
  verificarAlertas(): Promise<number>;
  exportarCenso(ano: number): Promise<ArquivoBaixado>;
  exportarSistemaPresenca(
    ano: number,
    bimestre: number,
  ): Promise<ArquivoBaixado>;
}
