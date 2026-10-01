"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { anoAtual } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

/** RF-09: vagas por turma, com bloqueio automático ao atingir a capacidade. */
export function OcupacaoTurmas() {
  const { matricula } = useGateways();
  const estado = useCarregar(() => matricula.ocupacao(anoAtual()), []);
  return (
    <Card size="sm">
      <CardHeader>
        <CardTitle>Ocupação das turmas {anoAtual()}</CardTitle>
      </CardHeader>
      <CardContent>
        <Conteudo
          estado={estado}
          vazio={(l) => l.length === 0}
          mensagemVazio="Nenhuma turma cadastrada para o ano."
        >
          {(turmas) => (
            <ul className="grid gap-3 sm:grid-cols-3 lg:grid-cols-5">
              {turmas.map(({ turma, ocupadas, vagas }) => {
                const percentual = Math.round(
                  (ocupadas / turma.capacidadeMaxima) * 100,
                );
                return (
                  <li key={turma.id} className="space-y-1">
                    <div className="flex justify-between text-xs">
                      <span className="font-medium">{turma.nome}</span>
                      <span
                        className={
                          vagas === 0
                            ? "text-destructive"
                            : "text-muted-foreground"
                        }
                      >
                        {vagas === 0 ? "Lotada" : `${vagas} vagas`}
                      </span>
                    </div>
                    <div
                      className="h-1.5 overflow-hidden rounded-full bg-muted"
                      aria-hidden
                    >
                      <div
                        className="h-full rounded-full bg-primary"
                        style={{ width: `${percentual}%` }}
                      />
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </Conteudo>
      </CardContent>
    </Card>
  );
}
