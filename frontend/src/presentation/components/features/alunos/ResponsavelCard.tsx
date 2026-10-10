"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { KeyRound, Pencil } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import type { z } from "zod";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { ConviteAcesso } from "@/domain/models/auth";
import type { AlunoDetalhe } from "@/domain/models/comum";
import { FormularioDialog } from "@/presentation/components/features/cadastros/FormularioDialog";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { LinkAcessoDialog } from "@/presentation/components/shared/LinkAcessoDialog";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { responsavelSchema } from "@/presentation/schemas/cadastros";

type Form = z.infer<typeof responsavelSchema>;

/** Dados do responsável financeiro, edição e link de acesso ao portal. */
export function ResponsavelCard({
  aluno,
  aoAlterar,
}: {
  aluno: AlunoDetalhe;
  aoAlterar: () => Promise<void>;
}) {
  const { cadastro } = useGateways();
  const r = aluno.responsavel;
  const [editando, setEditando] = useState(false);
  const [convite, setConvite] = useState<ConviteAcesso | null>(null);
  const { register, handleSubmit, reset, formState } = useForm<Form>({
    resolver: zodResolver(responsavelSchema),
  });
  const salvar = useAcao(async (dados: Form) => {
    await cadastro.atualizarResponsavel(aluno.id, dados);
    setEditando(false);
    await aoAlterar();
  }, "Dados do responsável atualizados");
  const acesso = useAcao(async () => {
    setConvite(await cadastro.acessoResponsavel(aluno.id));
  });

  const e = formState.errors;
  return (
    <Card size="sm">
      <CardHeader>
        <CardTitle>Responsável</CardTitle>
        {r && (
          <CardAction>
            <Button
              size="icon-sm"
              variant="ghost"
              aria-label="Editar responsável"
              onClick={() => {
                reset({
                  nome: r.nome,
                  email: r.email ?? "",
                  telefone: r.telefone ?? "",
                  parentesco: r.parentesco,
                });
                setEditando(true);
              }}
            >
              <Pencil />
            </Button>
          </CardAction>
        )}
      </CardHeader>
      <CardContent className="space-y-1 text-sm">
        {r ? (
          <>
            <p className="font-medium">
              {r.nome} ({r.parentesco})
            </p>
            <p>CPF {r.cpf}</p>
            <p>{r.email || "—"}</p>
            <p>{r.telefone || "—"}</p>
            <Button
              size="sm"
              variant="outline"
              className="mt-2"
              disabled={!r.email || acesso.executando}
              onClick={() => acesso.executar()}
            >
              <KeyRound /> Link de acesso ao portal
            </Button>
          </>
        ) : (
          <p>—</p>
        )}
      </CardContent>
      <FormularioDialog
        aberto={editando}
        titulo="Editar responsável"
        aoFechar={() => setEditando(false)}
        aoEnviar={handleSubmit((d) => salvar.executar(d))}
        enviando={salvar.executando}
      >
        <CampoTexto
          id="resp-nome"
          rotulo="Nome"
          erro={e.nome?.message}
          {...register("nome")}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <CampoTexto
            id="resp-email"
            rotulo="E-mail"
            type="email"
            erro={e.email?.message}
            {...register("email")}
          />
          <CampoTexto
            id="resp-tel"
            rotulo="Telefone"
            erro={e.telefone?.message}
            {...register("telefone")}
          />
        </div>
        <CampoTexto
          id="resp-par"
          rotulo="Parentesco"
          erro={e.parentesco?.message}
          {...register("parentesco")}
        />
        <p className="text-xs text-muted-foreground">
          O CPF não pode ser alterado: ele vincula o responsável ao portal.
        </p>
      </FormularioDialog>
      <LinkAcessoDialog convite={convite} aoFechar={() => setConvite(null)} />
    </Card>
  );
}
