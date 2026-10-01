"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FieldGroup } from "@/components/ui/field";
import { Campo } from "@/presentation/components/shared/Campo";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { useGateways } from "@/presentation/providers/GatewayProvider";

/** RF-08: renovação reaproveita os dados e sinaliza só documentos vencidos. */
export function RenovacaoDialog({
  aberto,
  aoFechar,
  aoConcluir,
}: {
  aberto: boolean;
  aoFechar: () => void;
  aoConcluir: () => void;
}) {
  const { cadastro, matricula } = useGateways();
  const alunos = useCarregar(() => cadastro.alunos("ATIVO"), [], aberto);
  const turmas = useCarregar(() => cadastro.turmas(), [], aberto);
  const [alunoId, setAlunoId] = useState<string>();
  const [turmaId, setTurmaId] = useState<string>();
  const renovar = useAcao(async () => {
    await matricula.renovar(alunoId as string, turmaId as string);
    aoConcluir();
    aoFechar();
  }, "Renovação registrada – aguardando análise");

  return (
    <Dialog open={aberto} onOpenChange={(a) => !a && aoFechar()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Renovar matrícula</DialogTitle>
          <DialogDescription>
            Para alunos já cadastrados, no próximo ano letivo.
          </DialogDescription>
        </DialogHeader>
        <FieldGroup>
          <Campo id="renov-aluno" rotulo="Aluno">
            <Selecao
              id="renov-aluno"
              valor={alunoId}
              aoMudar={setAlunoId}
              opcoes={(alunos.dados ?? []).map((a) => ({
                valor: a.id,
                rotulo: a.nome,
              }))}
            />
          </Campo>
          <Campo id="renov-turma" rotulo="Nova turma">
            <Selecao
              id="renov-turma"
              valor={turmaId}
              aoMudar={setTurmaId}
              opcoes={(turmas.dados ?? []).map((t) => ({
                valor: t.id,
                rotulo: `${t.nome} (${t.anoLetivo})`,
              }))}
            />
          </Campo>
        </FieldGroup>
        <DialogFooter>
          <Button variant="outline" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button
            disabled={!alunoId || !turmaId || renovar.executando}
            onClick={() => renovar.executar()}
          >
            Renovar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
