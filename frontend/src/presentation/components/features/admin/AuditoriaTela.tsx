"use client";

import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { dataHora } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

/** RF-36 / RNF-06: log de auditoria imutável. */
export function AuditoriaTela() {
  const { cadastro } = useGateways();
  const [entidade, setEntidade] = useState("");
  const estado = useCarregar(
    () => cadastro.auditoria({ entidade: entidade || undefined }),
    [entidade],
  );
  return (
    <>
      <PageHeader
        titulo="Auditoria"
        descricao="Quem fez o quê, quando e em qual registro (LGPD)"
      />
      <Input
        aria-label="Filtrar por entidade"
        placeholder="Entidade (ex.: Matricula, Contrato, Aluno)"
        className="max-w-sm"
        value={entidade}
        onChange={(e) => setEntidade(e.target.value)}
      />
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(r) => r.id}
            colunas={[
              { titulo: "Quando", celula: (r) => dataHora(r.ocorridoEm) },
              { titulo: "Usuário", celula: (r) => r.usuario },
              {
                titulo: "Ação",
                celula: (r) => <StatusBadge status={r.acao} />,
              },
              { titulo: "Entidade", celula: (r) => r.entidade },
              {
                titulo: "Detalhes",
                celula: (r) => (
                  <span className="text-xs text-muted-foreground">
                    {r.detalhes}
                  </span>
                ),
              },
            ]}
          />
        )}
      </Conteudo>
    </>
  );
}
