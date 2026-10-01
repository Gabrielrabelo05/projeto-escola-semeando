"use client";

import { Selecao } from "@/presentation/components/shared/Selecao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { anoAtual } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

export function SeletorTurma({
  valor,
  aoMudar,
}: {
  valor: string | undefined;
  aoMudar: (id: string) => void;
}) {
  const { cadastro } = useGateways();
  const turmas = useCarregar(() => cadastro.turmas(anoAtual()), []);
  return (
    <Selecao
      valor={valor}
      aoMudar={aoMudar}
      placeholder="Selecione a turma"
      className="w-56"
      opcoes={(turmas.dados ?? []).map((t) => ({
        valor: t.id,
        rotulo: t.nome,
      }))}
    />
  );
}
