import type {
  PedagogicoGateway,
  Periodo,
} from "@/domain/gateways/PedagogicoGateway";
import type { AlunoResumo, UUID } from "@/domain/models/comum";
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
} from "@/domain/models/pedagogico";
import type { HttpClient } from "../http/HttpClient";

const BASE = "/pedagogico";

export class HttpPedagogicoGateway implements PedagogicoGateway {
  constructor(private readonly http: HttpClient) {}

  planos(filtro: {
    turmaId?: UUID;
    status?: StatusPlanoAula;
  }): Promise<PlanoAula[]> {
    return this.http.get(`${BASE}/planos-aula`, filtro);
  }

  salvarPlano(dados: PlanoAulaForm, id?: UUID): Promise<PlanoAula> {
    return id
      ? this.http.put(`${BASE}/planos-aula/${id}`, dados)
      : this.http.post(`${BASE}/planos-aula`, dados);
  }

  submeterPlano(id: UUID): Promise<PlanoAula> {
    return this.http.post(`${BASE}/planos-aula/${id}/submeter`);
  }

  aprovarPlano(id: UUID, comentario?: string): Promise<PlanoAula> {
    return this.http.post(`${BASE}/planos-aula/${id}/aprovar`, { comentario });
  }

  devolverPlano(id: UUID, comentario: string): Promise<PlanoAula> {
    return this.http.post(`${BASE}/planos-aula/${id}/devolver`, { comentario });
  }

  chamada(turmaId: UUID): Promise<AlunoResumo[]> {
    return this.http.get(`${BASE}/turmas/${turmaId}/chamada`);
  }

  registrarDiario(dados: DiarioForm): Promise<DiarioClasse> {
    return this.http.post(`${BASE}/diarios`, dados);
  }

  diarios(turmaId: UUID, periodo: Periodo): Promise<DiarioClasse[]> {
    return this.http.get(`${BASE}/turmas/${turmaId}/diarios`, { ...periodo });
  }

  frequenciaTurma(turmaId: UUID, periodo: Periodo): Promise<Frequencia[]> {
    return this.http.get(`${BASE}/turmas/${turmaId}/frequencia`, {
      ...periodo,
    });
  }

  planejamentos(turmaId?: UUID): Promise<Planejamento[]> {
    return this.http.get(`${BASE}/planejamentos`, { turmaId });
  }

  registrarPlanejamento(dados: {
    turmaId: UUID;
    data: string;
    roteiro: string;
  }): Promise<Planejamento> {
    return this.http.post(`${BASE}/planejamentos`, dados);
  }

  comentarPlanejamento(id: UUID, comentario: string): Promise<Planejamento> {
    return this.http.post(`${BASE}/planejamentos/${id}/comentar`, {
      comentario,
    });
  }

  calendario(ano: number): Promise<EventoCalendario[]> {
    return this.http.get(`${BASE}/calendario`, { ano });
  }

  publicarEvento(dados: EventoForm): Promise<EventoCalendario> {
    return this.http.post(`${BASE}/calendario`, dados);
  }

  removerEvento(id: UUID): Promise<void> {
    return this.http.delete(`${BASE}/calendario/${id}`);
  }

  formacoes(professorId?: UUID): Promise<Formacao[]> {
    return this.http.get(`${BASE}/formacoes`, { professorId });
  }

  registrarFormacao(dados: FormacaoForm): Promise<Formacao> {
    return this.http.post(`${BASE}/formacoes`, dados);
  }
}
