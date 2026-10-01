"use client";

import { Download } from "lucide-react";
import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import {
  anoAtual,
  baixarArquivo,
  hojeISO,
} from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { SeletorTurma } from "./SeletorTurma";

/** RF-44 (alerta < 75%), RF-45 (visão da turma) e RF-50 (exportação para o Sistema Presença). */
export function FrequenciaAba() {
  const { pedagogico, obrigacao } = useGateways();
  const [turmaId, setTurmaId] = useState<string>();
  const [inicio, setInicio] = useState(`${anoAtual()}-01-01`);
  const [fim, setFim] = useState(hojeISO());
  const estado = useCarregar(
    () => pedagogico.frequenciaTurma(turmaId as string, { inicio, fim }),
    [turmaId, inicio, fim],
    !!turmaId,
  );
  const exportar = useAcao(async (bimestre: number) =>
    baixarArquivo(
      await obrigacao.exportarSistemaPresenca(anoAtual(), bimestre),
    ),
  );

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center gap-2">
        <SeletorTurma valor={turmaId} aoMudar={setTurmaId} />
        <Input
          type="date"
          aria-label="Início"
          className="w-40"
          value={inicio}
          onChange={(e) => setInicio(e.target.value)}
        />
        <Input
          type="date"
          aria-label="Fim"
          className="w-40"
          value={fim}
          onChange={(e) => setFim(e.target.value)}
        />
        {[1, 2, 3, 4].map((b) => (
          <Button
            key={b}
            size="sm"
            variant="outline"
            onClick={() => exportar.executar(b)}
          >
            <Download /> {b}º bim.
          </Button>
        ))}
      </div>
      {turmaId && (
        <Conteudo
          estado={estado}
          vazio={(l) => l.length === 0}
          mensagemVazio="Nenhuma aula registrada no período."
        >
          {(lista) => (
            <Tabela
              linhas={lista}
              chave={(f) => f.alunoId}
              colunas={[
                {
                  titulo: "Aluno",
                  celula: (f) => (
                    <span className="font-medium">{f.nomeAluno}</span>
                  ),
                },
                { titulo: "Aulas", celula: (f) => f.aulas },
                { titulo: "Faltas", celula: (f) => f.faltas },
                {
                  titulo: "Frequência",
                  celula: (f) => `${f.percentual.toFixed(1)}%`,
                },
                {
                  titulo: "",
                  celula: (f) =>
                    f.alerta ? (
                      <Badge variant="destructive">Abaixo de 75%</Badge>
                    ) : (
                      <Badge variant="outline">OK</Badge>
                    ),
                },
              ]}
            />
          )}
        </Conteudo>
      )}
    </div>
  );
}
