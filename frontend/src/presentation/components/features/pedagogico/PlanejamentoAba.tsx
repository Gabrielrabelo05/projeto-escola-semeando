"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Textarea } from "@/components/ui/textarea";
import type { Planejamento } from "@/domain/models/pedagogico";
import { Confirmacao } from "@/presentation/components/shared/Confirmacao";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, hojeISO } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { SeletorTurma } from "./SeletorTurma";

/** RF-41: roteiro diário do professor, com comentários da coordenação. */
export function PlanejamentoAba() {
  const { pedagogico } = useGateways();
  const { possuiPerfil } = useAuth();
  const professor = possuiPerfil("PROFESSOR");
  const [turmaId, setTurmaId] = useState<string>();
  const [roteiro, setRoteiro] = useState("");
  const [comentando, setComentando] = useState<Planejamento | null>(null);
  const estado = useCarregar(
    () => pedagogico.planejamentos(turmaId),
    [turmaId],
  );
  const registrar = useAcao(async () => {
    await pedagogico.registrarPlanejamento({
      turmaId: turmaId as string,
      data: hojeISO(),
      roteiro,
    });
    setRoteiro("");
    await estado.recarregar();
  }, "Planejamento registrado");

  return (
    <div className="space-y-3">
      <SeletorTurma valor={turmaId} aoMudar={setTurmaId} />
      {professor && turmaId && (
        <div className="flex max-w-xl flex-col gap-2">
          <Textarea
            aria-label="Roteiro do dia"
            placeholder="Roteiro de atividades de hoje…"
            value={roteiro}
            onChange={(e) => setRoteiro(e.target.value)}
          />
          <Button
            className="w-fit"
            disabled={!roteiro.trim()}
            onClick={() => registrar.executar()}
          >
            Registrar roteiro
          </Button>
        </div>
      )}
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhum planejamento registrado."
      >
        {(lista) => (
          <div className="grid gap-3 md:grid-cols-2">
            {lista.map((p) => (
              <Card key={p.id} size="sm">
                <CardHeader>
                  <CardTitle>{data(p.data)}</CardTitle>
                  {p.comentarioCoordenacao && (
                    <CardDescription>
                      Coordenação: {p.comentarioCoordenacao}
                    </CardDescription>
                  )}
                </CardHeader>
                <CardContent className="space-y-2">
                  <p className="text-sm whitespace-pre-wrap">{p.roteiro}</p>
                  {!professor && (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setComentando(p)}
                    >
                      Comentar
                    </Button>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </Conteudo>
      <Confirmacao
        aberto={comentando !== null}
        aoFechar={() => setComentando(null)}
        titulo="Comentar planejamento"
        pedirTexto="Comentário"
        aoConfirmar={async (comentario) => {
          await pedagogico.comentarPlanejamento(
            comentando?.id as string,
            comentario,
          );
          await estado.recarregar();
        }}
      />
    </div>
  );
}
