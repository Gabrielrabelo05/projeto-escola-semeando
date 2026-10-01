"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { FieldGroup } from "@/components/ui/field";
import { Label } from "@/components/ui/label";
import {
  CampoAreaTexto,
  CampoTexto,
} from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { hojeISO } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { SeletorTurma } from "./SeletorTurma";

/** RF-40: chamada e conteúdo ministrado por aula. */
export function DiarioClasseAba() {
  const { pedagogico } = useGateways();
  const { possuiPerfil } = useAuth();
  const [turmaId, setTurmaId] = useState<string>();
  const [dia, setDia] = useState(hojeISO());
  const [conteudo, setConteudo] = useState("");
  const [presentes, setPresentes] = useState<Record<string, boolean>>({});
  const chamada = useCarregar(
    () => pedagogico.chamada(turmaId as string),
    [turmaId],
    !!turmaId,
  );

  useEffect(() => {
    setPresentes(
      Object.fromEntries((chamada.dados ?? []).map((a) => [a.id, true])),
    );
  }, [chamada.dados]);

  const salvar = useAcao(
    () =>
      pedagogico.registrarDiario({
        turmaId: turmaId as string,
        data: dia,
        conteudoMinistrado: conteudo,
        presencas: Object.entries(presentes).map(([alunoId, presente]) => ({
          alunoId,
          presente,
        })),
      }),
    "Diário registrado",
  );

  if (!possuiPerfil("PROFESSOR")) {
    return (
      <p className="text-sm text-muted-foreground">
        O registro do diário é feito pelo professor. Consulte a frequência na
        aba ao lado.
      </p>
    );
  }

  return (
    <div className="space-y-4">
      <SeletorTurma valor={turmaId} aoMudar={setTurmaId} />
      {turmaId && (
        <Conteudo
          estado={chamada}
          vazio={(l) => l.length === 0}
          mensagemVazio="Nenhum aluno ativo nesta turma."
        >
          {(alunos) => (
            <FieldGroup className="max-w-xl">
              <CampoTexto
                id="diario-data"
                rotulo="Data"
                type="date"
                value={dia}
                onChange={(e) => setDia(e.target.value)}
              />
              <CampoAreaTexto
                id="diario-conteudo"
                rotulo="Conteúdo ministrado"
                value={conteudo}
                onChange={(e) => setConteudo(e.target.value)}
              />
              <fieldset className="space-y-2 rounded-lg border p-3">
                <legend className="px-1 text-sm font-medium">Chamada</legend>
                {alunos.map((a) => (
                  <div key={a.id} className="flex items-center gap-2">
                    <Checkbox
                      id={`p-${a.id}`}
                      checked={presentes[a.id] ?? true}
                      onCheckedChange={(v) =>
                        setPresentes((p) => ({ ...p, [a.id]: v === true }))
                      }
                    />
                    <Label htmlFor={`p-${a.id}`}>{a.nome}</Label>
                  </div>
                ))}
              </fieldset>
              <Button
                disabled={!conteudo.trim() || salvar.executando}
                onClick={() => salvar.executar()}
              >
                Salvar diário
              </Button>
            </FieldGroup>
          )}
        </Conteudo>
      )}
    </div>
  );
}
