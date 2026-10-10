"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import type { z } from "zod";
import { Button } from "@/components/ui/button";
import type { Professor } from "@/domain/models/cadastro";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { professorSchema } from "@/presentation/schemas/cadastros";
import { BotaoAcesso } from "./BotaoAcesso";
import { FormularioDialog } from "./FormularioDialog";

type Form = z.input<typeof professorSchema>;
const VAZIO: Form = {
  nome: "",
  cpf: "",
  email: "",
  telefone: "",
  rg: "",
  formacaoAcademica: "",
  disciplinas: "",
  cargaHorariaSemanal: "40",
};

/** RF-32 */
export function ProfessoresAba() {
  const { cadastro } = useGateways();
  const estado = useCarregar(() => cadastro.professores(true), []);
  const [editando, setEditando] = useState<Professor | "novo" | null>(null);
  const { register, handleSubmit, reset, formState } = useForm<
    Form,
    unknown,
    z.output<typeof professorSchema>
  >({
    resolver: zodResolver(professorSchema),
  });
  const salvar = useAcao(async (dados: z.output<typeof professorSchema>) => {
    await cadastro.salvarProfessor(
      dados,
      editando !== "novo" ? editando?.id : undefined,
    );
    setEditando(null);
    await estado.recarregar();
  }, "Professor salvo");
  const inativar = useAcao(async (id: string) => {
    await cadastro.inativarProfessor(id);
    await estado.recarregar();
  }, "Professor inativado");

  function abrir(professor: Professor | "novo") {
    reset(
      professor === "novo"
        ? VAZIO
        : {
            ...professor,
            email: professor.email ?? "",
            telefone: professor.telefone ?? "",
            rg: professor.rg ?? "",
            formacaoAcademica: professor.formacaoAcademica ?? "",
            disciplinas: professor.disciplinas.join(", "),
            cargaHorariaSemanal: String(professor.cargaHorariaSemanal),
          },
    );
    setEditando(professor);
  }

  const e = formState.errors;
  return (
    <div className="space-y-3">
      <Button onClick={() => abrir("novo")}>
        <Plus /> Novo professor
      </Button>
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(p) => p.id}
            aoClicar={abrir}
            colunas={[
              {
                titulo: "Nome",
                celula: (p) => <span className="font-medium">{p.nome}</span>,
              },
              { titulo: "CPF", celula: (p) => p.cpf },
              {
                titulo: "Disciplinas",
                celula: (p) => p.disciplinas.join(", ") || "—",
              },
              {
                titulo: "Carga horária",
                celula: (p) => `${p.cargaHorariaSemanal}h`,
              },
              {
                titulo: "Situação",
                celula: (p) => (
                  <StatusBadge status={p.ativo ? "ATIVO" : "INATIVO"} />
                ),
              },
              {
                titulo: "",
                celula: (p) =>
                  p.ativo && (
                    <div className="flex justify-end gap-1">
                      <BotaoAcesso
                        nome={p.nome}
                        email={p.email}
                        perfil="PROFESSOR"
                        cpf={p.cpf}
                      />
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={(ev) => {
                          ev.stopPropagation();
                          void inativar.executar(p.id);
                        }}
                      >
                        Inativar
                      </Button>
                    </div>
                  ),
              },
            ]}
          />
        )}
      </Conteudo>
      <FormularioDialog
        aberto={editando !== null}
        titulo={editando === "novo" ? "Novo professor" : "Editar professor"}
        aoFechar={() => setEditando(null)}
        aoEnviar={handleSubmit((d) => salvar.executar(d))}
        enviando={salvar.executando}
      >
        <CampoTexto
          id="prof-nome"
          rotulo="Nome"
          erro={e.nome?.message}
          {...register("nome")}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <CampoTexto
            id="prof-cpf"
            rotulo="CPF"
            disabled={editando !== "novo"}
            erro={e.cpf?.message}
            {...register("cpf")}
          />
          <CampoTexto id="prof-rg" rotulo="RG" {...register("rg")} />
          <CampoTexto
            id="prof-email"
            rotulo="E-mail"
            erro={e.email?.message}
            {...register("email")}
          />
          <CampoTexto
            id="prof-tel"
            rotulo="Telefone"
            {...register("telefone")}
          />
        </div>
        <CampoTexto
          id="prof-formacao"
          rotulo="Formação acadêmica"
          {...register("formacaoAcademica")}
        />
        <CampoTexto
          id="prof-disc"
          rotulo="Disciplinas"
          descricao="Separadas por vírgula"
          {...register("disciplinas")}
        />
        <CampoTexto
          id="prof-ch"
          rotulo="Carga horária semanal"
          inputMode="numeric"
          erro={e.cargaHorariaSemanal?.message}
          {...register("cargaHorariaSemanal")}
        />
      </FormularioDialog>
    </div>
  );
}
