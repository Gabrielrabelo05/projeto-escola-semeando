import type { AlunoResumo, UUID } from "../models/comum";
import type {
  DiarioClasse,
  DiarioForm,
  EventoCalendario,
  EventoForm,
  Formacao,
  FormacaoForm,
  Frequencia,
  Planejamento,
  PlanoAula,
  PlanoAulaForm,
  StatusPlanoAula,
} from "../models/pedagogico";

export interface Periodo {
  inicio: string;
  fim: string;
}

export interface PedagogicoGateway {
  planos(filtro: {
    turmaId?: UUID;
    status?: StatusPlanoAula;
  }): Promise<PlanoAula[]>;
  salvarPlano(dados: PlanoAulaForm, id?: UUID): Promise<PlanoAula>;
  submeterPlano(id: UUID): Promise<PlanoAula>;
  aprovarPlano(id: UUID, comentario?: string): Promise<PlanoAula>;
  devolverPlano(id: UUID, comentario: string): Promise<PlanoAula>;

  chamada(turmaId: UUID): Promise<AlunoResumo[]>;
  registrarDiario(dados: DiarioForm): Promise<DiarioClasse>;
  diarios(turmaId: UUID, periodo: Periodo): Promise<DiarioClasse[]>;
  frequenciaTurma(turmaId: UUID, periodo: Periodo): Promise<Frequencia[]>;

  planejamentos(turmaId?: UUID): Promise<Planejamento[]>;
  registrarPlanejamento(dados: {
    turmaId: UUID;
    data: string;
    roteiro: string;
  }): Promise<Planejamento>;
  comentarPlanejamento(id: UUID, comentario: string): Promise<Planejamento>;

  calendario(ano: number): Promise<EventoCalendario[]>;
  publicarEvento(dados: EventoForm): Promise<EventoCalendario>;
  removerEvento(id: UUID): Promise<void>;

  formacoes(professorId?: UUID): Promise<Formacao[]>;
  registrarFormacao(dados: FormacaoForm): Promise<Formacao>;
}
