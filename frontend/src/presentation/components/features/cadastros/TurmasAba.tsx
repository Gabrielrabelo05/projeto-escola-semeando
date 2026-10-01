"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus } from "lucide-react";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import type { z } from "zod";
import { Button } from "@/components/ui/button";
import { SERIES, type Turma } from "@/domain/models/comum";
import { Campo, CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { anoAtual, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { turmaSchema } from "@/presentation/schemas/cadastros";
import { FormularioDialog } from "./FormularioDialog";

type Form = z.input<typeof turmaSchema>;

export function TurmasAba() {
  const { cadastro } = useGateways();
  const estado = useCarregar(() => cadastro.turmas(), []);
  const professores = useCarregar(() => cadastro.professores(), []);
  const [editando, setEditando] = useState<Turma | "novo" | null>(null);
  const [professorId, setProfessorId] = useState<string>();
  const { register, control, handleSubmit, reset, formState } = useForm<
    Form,
    unknown,
    z.output<typeof turmaSchema>
  >({
    resolver: zodResolver(turmaSchema),
  });
  const salvar = useAcao(async (dados: z.output<typeof turmaSchema>) => {
    await cadastro.salvarTurma(
      { ...dados, professorTitularId: professorId ?? null },
      editando !== "novo" ? editando?.id : undefined,
    );
    setEditando(null);
    await estado.recarregar();
  }, "Turma salva");

  function abrir(t: Turma | "novo") {
    reset(
      t === "novo"
        ? {
            nome: "",
            serie: "PRIMEIRO_ANO",
            anoLetivo: String(anoAtual() + 1),
            capacidadeMaxima: "20",
          }
        : {
            nome: t.nome,
            serie: t.serie,
            anoLetivo: String(t.anoLetivo),
            capacidadeMaxima: String(t.capacidadeMaxima),
          },
    );
    setProfessorId(
      t === "novo" ? undefined : (t.professorTitularId ?? undefined),
    );
    setEditando(t);
  }

  const nomeProfessor = (id: string | null) =>
    professores.dados?.find((p) => p.id === id)?.nome ?? "—";
  const e = formState.errors;
  return (
    <div className="space-y-3">
      <Button onClick={() => abrir("novo")}>
        <Plus /> Nova turma
      </Button>
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(t) => t.id}
            aoClicar={abrir}
            colunas={[
              {
                titulo: "Turma",
                celula: (t) => <span className="font-medium">{t.nome}</span>,
              },
              { titulo: "Série", celula: (t) => t.serieDescricao },
              { titulo: "Nível", celula: (t) => rotulo(t.nivel) },
              { titulo: "Ano", celula: (t) => t.anoLetivo },
              { titulo: "Capacidade", celula: (t) => t.capacidadeMaxima },
              {
                titulo: "Professor titular",
                celula: (t) => nomeProfessor(t.professorTitularId),
              },
            ]}
          />
        )}
      </Conteudo>
      <FormularioDialog
        aberto={editando !== null}
        titulo={editando === "novo" ? "Nova turma" : "Editar turma"}
        aoFechar={() => setEditando(null)}
        aoEnviar={handleSubmit((d) => salvar.executar(d))}
        enviando={salvar.executando}
      >
        <CampoTexto
          id="turma-nome"
          rotulo="Nome"
          erro={e.nome?.message}
          {...register("nome")}
        />
        <Controller
          control={control}
          name="serie"
          render={({ field }) => (
            <Campo id="turma-serie" rotulo="Série">
              <Selecao
                id="turma-serie"
                valor={field.value}
                aoMudar={field.onChange}
                opcoes={SERIES.map((s) => ({
                  valor: s.valor,
                  rotulo: s.descricao,
                }))}
              />
            </Campo>
          )}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <CampoTexto
            id="turma-ano"
            rotulo="Ano letivo"
            disabled={editando !== "novo"}
            erro={e.anoLetivo?.message}
            {...register("anoLetivo")}
          />
          <CampoTexto
            id="turma-cap"
            rotulo="Capacidade máxima"
            erro={e.capacidadeMaxima?.message}
            {...register("capacidadeMaxima")}
          />
        </div>
        <Campo id="turma-prof" rotulo="Professor titular">
          <Selecao
            id="turma-prof"
            valor={professorId}
            aoMudar={setProfessorId}
            opcoes={(professores.dados ?? []).map((p) => ({
              valor: p.id,
              rotulo: p.nome,
            }))}
          />
        </Campo>
      </FormularioDialog>
    </div>
  );
}
