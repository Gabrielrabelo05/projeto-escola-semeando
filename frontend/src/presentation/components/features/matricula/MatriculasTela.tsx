"use client";

import { RefreshCw } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import type { Matricula, StatusMatricula } from "@/domain/models/matricula";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { anoAtual, data, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { MatriculaDetalhe } from "./MatriculaDetalhe";
import { OcupacaoTurmas } from "./OcupacaoTurmas";
import { RenovacaoDialog } from "./RenovacaoDialog";

const STATUS: StatusMatricula[] = [
  "AGUARDANDO_ANALISE",
  "APROVADA",
  "ATIVA",
  "REJEITADA",
  "TRANSFERIDA",
  "CANCELADA",
];
const TODOS = "TODOS";

/** Módulo 1 – Matrícula (RF-02 a RF-12). */
export function MatriculasTela() {
  const { matricula } = useGateways();
  const [status, setStatus] = useState<string>("AGUARDANDO_ANALISE");
  const [selecionada, setSelecionada] = useState<Matricula | null>(null);
  const [renovando, setRenovando] = useState(false);
  const estado = useCarregar(
    () =>
      matricula.listar({
        status: status === TODOS ? undefined : (status as StatusMatricula),
        anoLetivo: status === TODOS ? anoAtual() : undefined,
      }),
    [status],
  );

  const opcoes = [
    ...STATUS.map((s) => ({ valor: s, rotulo: rotulo(s) })),
    { valor: TODOS, rotulo: `Todas de ${anoAtual()}` },
  ];

  return (
    <>
      <PageHeader
        titulo="Matrículas"
        descricao="Análise de pré-matrículas, documentos, farda, transferências e renovações"
        acoes={
          <Button variant="outline" onClick={() => setRenovando(true)}>
            <RefreshCw /> Renovar matrícula
          </Button>
        }
      />
      <OcupacaoTurmas />
      <div className="flex items-center gap-2">
        <span className="text-sm text-muted-foreground">Status</span>
        <Selecao
          valor={status}
          opcoes={opcoes}
          aoMudar={setStatus}
          className="w-56"
        />
      </div>
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhuma matrícula com este status."
      >
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(m) => m.id}
            aoClicar={setSelecionada}
            colunas={[
              {
                titulo: "Aluno",
                celula: (m) => (
                  <span className="font-medium">{m.aluno?.nome}</span>
                ),
              },
              { titulo: "Turma", celula: (m) => m.turma?.nome ?? "—" },
              { titulo: "Tipo", celula: (m) => rotulo(m.tipo) },
              {
                titulo: "Solicitada em",
                celula: (m) => data(m.dataSolicitacao),
              },
              {
                titulo: "Pendências",
                celula: (m) => m.documentos.filter((d) => d.pendente).length,
              },
              {
                titulo: "Status",
                celula: (m) => <StatusBadge status={m.status} />,
              },
            ]}
          />
        )}
      </Conteudo>
      <MatriculaDetalhe
        matriculaId={selecionada?.id ?? null}
        aoFechar={() => setSelecionada(null)}
        aoAlterar={estado.recarregar}
      />
      <RenovacaoDialog
        aberto={renovando}
        aoFechar={() => setRenovando(false)}
        aoConcluir={estado.recarregar}
      />
    </>
  );
}
