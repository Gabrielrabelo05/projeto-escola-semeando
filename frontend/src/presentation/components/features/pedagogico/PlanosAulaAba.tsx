"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import type { PlanoAula } from "@/domain/models/pedagogico";
import { FormularioDialog } from "@/presentation/components/features/cadastros/FormularioDialog";
import {
  CampoAreaTexto,
  CampoTexto,
} from "@/presentation/components/shared/Campo";
import { Confirmacao } from "@/presentation/components/shared/Confirmacao";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, hojeISO } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { SeletorTurma } from "./SeletorTurma";

const schema = z.object({
  data: z.string().min(1, "Informe a data"),
  objetivo: z.string().trim().min(1, "Obrigatório"),
  conteudo: z.string().trim().min(1, "Obrigatório"),
  metodologia: z.string().trim().min(1, "Obrigatório"),
  avaliacao: z.string(),
  recursos: z.string(),
});
type Form = z.infer<typeof schema>;

/** RF-39 (professor cria e submete) e RF-46 (coordenação aprova/devolve). */
export function PlanosAulaAba() {
  const { pedagogico } = useGateways();
  const { possuiPerfil } = useAuth();
  const professor = possuiPerfil("PROFESSOR");
  const [turmaId, setTurmaId] = useState<string>();
  const estado = useCarregar(() => pedagogico.planos({ turmaId }), [turmaId]);
  const [editando, setEditando] = useState<PlanoAula | "novo" | null>(null);
  const [devolvendo, setDevolvendo] = useState<PlanoAula | null>(null);
  const { register, handleSubmit, reset, formState } = useForm<Form>({
    resolver: zodResolver(schema),
  });

  const salvar = useAcao(async (dados: Form) => {
    await pedagogico.salvarPlano(
      { ...dados, turmaId: turmaId as string },
      editando !== "novo" ? editando?.id : undefined,
    );
    setEditando(null);
    await estado.recarregar();
  }, "Plano salvo");
  const acao = useAcao(async (operacao: Promise<unknown>) => {
    await operacao;
    await estado.recarregar();
  }, "Plano atualizado");

  function abrir(plano: PlanoAula | "novo") {
    reset(
      plano === "novo"
        ? {
            data: hojeISO(),
            objetivo: "",
            conteudo: "",
            metodologia: "",
            avaliacao: "",
            recursos: "",
          }
        : {
            ...plano,
            avaliacao: plano.avaliacao ?? "",
            recursos: plano.recursos ?? "",
          },
    );
    setEditando(plano);
  }

  const e = formState.errors;
  return (
    <div className="space-y-3">
      <div className="flex flex-wrap gap-2">
        <SeletorTurma valor={turmaId} aoMudar={setTurmaId} />
        {professor && (
          <Button disabled={!turmaId} onClick={() => abrir("novo")}>
            <Plus /> Novo plano
          </Button>
        )}
      </div>
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhum plano de aula."
      >
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(p) => p.id}
            colunas={[
              { titulo: "Data", celula: (p) => data(p.data) },
              {
                titulo: "Objetivo",
                celula: (p) => (
                  <span className="line-clamp-1">{p.objetivo}</span>
                ),
              },
              {
                titulo: "Status",
                celula: (p) => <StatusBadge status={p.status} />,
              },
              {
                titulo: "Coordenação",
                celula: (p) => (
                  <span className="text-xs text-muted-foreground">
                    {p.comentarioCoordenacao ?? "—"}
                  </span>
                ),
              },
              {
                titulo: "Ações",
                celula: (p) => (
                  <div className="flex gap-1">
                    {professor &&
                      ["RASCUNHO", "DEVOLVIDO"].includes(p.status) && (
                        <>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => abrir(p)}
                          >
                            Editar
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() =>
                              acao.executar(pedagogico.submeterPlano(p.id))
                            }
                          >
                            Submeter
                          </Button>
                        </>
                      )}
                    {!professor && p.status === "SUBMETIDO" && (
                      <>
                        <Button
                          size="sm"
                          onClick={() =>
                            acao.executar(
                              pedagogico.aprovarPlano(p.id, "Aprovado"),
                            )
                          }
                        >
                          Aprovar
                        </Button>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => setDevolvendo(p)}
                        >
                          Devolver
                        </Button>
                      </>
                    )}
                  </div>
                ),
              },
            ]}
          />
        )}
      </Conteudo>
      <FormularioDialog
        aberto={editando !== null}
        titulo={
          editando === "novo" ? "Novo plano de aula" : "Editar plano de aula"
        }
        aoFechar={() => setEditando(null)}
        aoEnviar={handleSubmit((d) => salvar.executar(d))}
        enviando={salvar.executando}
      >
        <CampoTexto
          id="plano-data"
          rotulo="Data da aula"
          type="date"
          erro={e.data?.message}
          {...register("data")}
        />
        <CampoAreaTexto
          id="plano-obj"
          rotulo="Objetivo"
          erro={e.objetivo?.message}
          {...register("objetivo")}
        />
        <CampoAreaTexto
          id="plano-cont"
          rotulo="Conteúdo"
          erro={e.conteudo?.message}
          {...register("conteudo")}
        />
        <CampoAreaTexto
          id="plano-met"
          rotulo="Metodologia"
          erro={e.metodologia?.message}
          {...register("metodologia")}
        />
        <CampoAreaTexto
          id="plano-aval"
          rotulo="Avaliação"
          {...register("avaliacao")}
        />
        <CampoTexto
          id="plano-rec"
          rotulo="Recursos"
          {...register("recursos")}
        />
      </FormularioDialog>
      <Confirmacao
        aberto={devolvendo !== null}
        aoFechar={() => setDevolvendo(null)}
        titulo="Devolver plano para ajustes"
        pedirTexto="Comentário para o professor"
        rotuloConfirmar="Devolver"
        aoConfirmar={(comentario) =>
          acao.executar(
            pedagogico.devolverPlano(devolvendo?.id as string, comentario),
          )
        }
      />
    </div>
  );
}
