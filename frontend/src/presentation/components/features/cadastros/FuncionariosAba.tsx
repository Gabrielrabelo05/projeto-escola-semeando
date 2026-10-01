"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus } from "lucide-react";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import type { z } from "zod";
import { Button } from "@/components/ui/button";
import type { Funcionario } from "@/domain/models/cadastro";
import { Campo, CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { rotulo } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { funcionarioSchema } from "@/presentation/schemas/cadastros";
import { FormularioDialog } from "./FormularioDialog";

type Form = z.input<typeof funcionarioSchema>;
const PERFIS = [
  "SECRETARIA",
  "FINANCEIRO",
  "PROFESSOR",
  "DIRETOR",
  "ADMIN",
] as const;

/** RF-33 – somente diretoria/administração. */
export function FuncionariosAba() {
  const { cadastro } = useGateways();
  const { possuiPerfil } = useAuth();
  const gestao = possuiPerfil("ADMIN", "DIRETOR");
  const estado = useCarregar(() => cadastro.funcionarios(true), [], gestao);
  const [editando, setEditando] = useState<Funcionario | "novo" | null>(null);
  const { register, control, handleSubmit, reset, formState } = useForm<Form>({
    resolver: zodResolver(funcionarioSchema),
  });
  const salvar = useAcao(async (dados: Form) => {
    await cadastro.salvarFuncionario(
      dados,
      editando !== "novo" ? editando?.id : undefined,
    );
    setEditando(null);
    await estado.recarregar();
  }, "Funcionário salvo");

  if (!gestao)
    return (
      <p className="text-sm text-muted-foreground">
        Disponível apenas para a diretoria.
      </p>
    );

  function abrir(f: Funcionario | "novo") {
    reset(
      f === "novo"
        ? {
            nome: "",
            cpf: "",
            email: "",
            telefone: "",
            cargo: "",
            setor: "",
            perfilAcesso: "SECRETARIA",
          }
        : {
            ...f,
            email: f.email ?? "",
            telefone: f.telefone ?? "",
            perfilAcesso: f.perfilAcesso as Form["perfilAcesso"],
          },
    );
    setEditando(f);
  }

  const e = formState.errors;
  return (
    <div className="space-y-3">
      <Button onClick={() => abrir("novo")}>
        <Plus /> Novo funcionário
      </Button>
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(f) => f.id}
            aoClicar={abrir}
            colunas={[
              {
                titulo: "Nome",
                celula: (f) => <span className="font-medium">{f.nome}</span>,
              },
              { titulo: "Cargo", celula: (f) => f.cargo },
              { titulo: "Setor", celula: (f) => f.setor },
              { titulo: "Acesso", celula: (f) => rotulo(f.perfilAcesso) },
              {
                titulo: "Situação",
                celula: (f) => (
                  <StatusBadge status={f.ativo ? "ATIVO" : "INATIVO"} />
                ),
              },
            ]}
          />
        )}
      </Conteudo>
      <FormularioDialog
        aberto={editando !== null}
        titulo={editando === "novo" ? "Novo funcionário" : "Editar funcionário"}
        aoFechar={() => setEditando(null)}
        aoEnviar={handleSubmit((d) => salvar.executar(d))}
        enviando={salvar.executando}
      >
        <CampoTexto
          id="func-nome"
          rotulo="Nome"
          erro={e.nome?.message}
          {...register("nome")}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <CampoTexto
            id="func-cpf"
            rotulo="CPF"
            disabled={editando !== "novo"}
            erro={e.cpf?.message}
            {...register("cpf")}
          />
          <CampoTexto
            id="func-email"
            rotulo="E-mail"
            erro={e.email?.message}
            {...register("email")}
          />
          <CampoTexto
            id="func-cargo"
            rotulo="Cargo"
            erro={e.cargo?.message}
            {...register("cargo")}
          />
          <CampoTexto
            id="func-setor"
            rotulo="Setor"
            erro={e.setor?.message}
            {...register("setor")}
          />
        </div>
        <Controller
          control={control}
          name="perfilAcesso"
          render={({ field }) => (
            <Campo id="func-perfil" rotulo="Nível de acesso ao sistema">
              <Selecao
                id="func-perfil"
                valor={field.value}
                aoMudar={field.onChange}
                opcoes={PERFIS.map((p) => ({ valor: p, rotulo: rotulo(p) }))}
              />
            </Campo>
          )}
        />
      </FormularioDialog>
    </div>
  );
}
