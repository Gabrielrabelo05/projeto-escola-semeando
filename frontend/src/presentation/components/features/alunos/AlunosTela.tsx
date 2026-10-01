"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import type { StatusAluno } from "@/domain/models/comum";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

const STATUS: StatusAluno[] = [
  "ATIVO",
  "PRE_MATRICULA",
  "TRANSFERIDO",
  "CANCELADO",
  "INATIVO",
];

/** RF-31 – cadastro de alunos. */
export function AlunosTela() {
  const { cadastro } = useGateways();
  const router = useRouter();
  const [status, setStatus] = useState<StatusAluno>("ATIVO");
  const estado = useCarregar(() => cadastro.alunos(status), [status]);

  return (
    <>
      <PageHeader
        titulo="Alunos"
        descricao="Fichas completas com dados de saúde, autorizados e histórico"
      />
      <Selecao
        valor={status}
        aoMudar={(v) => setStatus(v as StatusAluno)}
        opcoes={STATUS.map((s) => ({ valor: s, rotulo: rotulo(s) }))}
        className="w-48"
      />
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(alunos) => (
          <Tabela
            linhas={alunos}
            chave={(a) => a.id}
            aoClicar={(a) => router.push(`/alunos/${a.id}`)}
            colunas={[
              {
                titulo: "Nome",
                celula: (a) => <span className="font-medium">{a.nome}</span>,
              },
              { titulo: "Nascimento", celula: (a) => data(a.dataNascimento) },
              {
                titulo: "Status",
                celula: (a) => <StatusBadge status={a.status} />,
              },
            ]}
          />
        )}
      </Conteudo>
    </>
  );
}
