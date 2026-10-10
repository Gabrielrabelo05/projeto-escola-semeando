"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus, Trash2 } from "lucide-react";
import { useEffect } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import type { z } from "zod";
import { Button } from "@/components/ui/button";
import { FieldError, FieldLegend, FieldSet } from "@/components/ui/field";
import type { AlunoDetalhe } from "@/domain/models/comum";
import { FormularioDialog } from "@/presentation/components/features/cadastros/FormularioDialog";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { alunoSchema } from "@/presentation/schemas/cadastros";

type Form = z.infer<typeof alunoSchema>;

interface Props {
  aluno: AlunoDetalhe;
  aberto: boolean;
  aoFechar: () => void;
  aoSalvar: () => Promise<void>;
}

/** RF-31: secretaria, direção e admin corrigem a ficha do aluno (nome, nascimento, saúde e segurança). */
export function EditarAlunoDialog({
  aluno,
  aberto,
  aoFechar,
  aoSalvar,
}: Props) {
  const { cadastro } = useGateways();
  const { register, control, handleSubmit, reset, formState } = useForm<Form>({
    resolver: zodResolver(alunoSchema),
  });
  const autorizados = useFieldArray({ control, name: "autorizados" });

  useEffect(() => {
    if (!aberto) return;
    reset({
      nome: aluno.nome,
      dataNascimento: aluno.dataNascimento,
      alergias: aluno.alergias ?? "",
      tipoSanguineo: aluno.tipoSanguineo ?? "",
      necessidadesEspeciais: aluno.necessidadesEspeciais ?? "",
      hospitalEmergencia: aluno.hospitalEmergencia ?? "",
      autorizados: aluno.autorizados.map((a) => ({
        ...a,
        telefone: a.telefone ?? "",
      })),
    });
  }, [aberto, aluno, reset]);

  const salvar = useAcao(async (dados: Form) => {
    await cadastro.atualizarAluno(aluno.id, {
      ...dados,
      fotoUrl: aluno.fotoUrl,
    });
    aoFechar();
    await aoSalvar();
  }, "Ficha do aluno atualizada");

  const e = formState.errors;
  return (
    <FormularioDialog
      aberto={aberto}
      titulo="Editar aluno"
      aoFechar={aoFechar}
      aoEnviar={handleSubmit((d) => salvar.executar(d))}
      enviando={salvar.executando}
    >
      <CampoTexto
        id="aluno-nome"
        rotulo="Nome"
        erro={e.nome?.message}
        {...register("nome")}
      />
      <div className="grid gap-4 sm:grid-cols-2">
        <CampoTexto
          id="aluno-nascimento"
          rotulo="Data de nascimento"
          type="date"
          erro={e.dataNascimento?.message}
          {...register("dataNascimento")}
        />
        <CampoTexto
          id="aluno-sangue"
          rotulo="Tipo sanguíneo"
          {...register("tipoSanguineo")}
        />
      </div>
      <CampoTexto
        id="aluno-alergias"
        rotulo="Alergias"
        {...register("alergias")}
      />
      <CampoTexto
        id="aluno-necessidades"
        rotulo="Necessidades especiais"
        {...register("necessidadesEspeciais")}
      />
      <CampoTexto
        id="aluno-hospital"
        rotulo="Hospital de emergência"
        erro={e.hospitalEmergencia?.message}
        {...register("hospitalEmergencia")}
      />
      <FieldSet>
        <FieldLegend variant="label">
          Autorizados a buscar a criança
        </FieldLegend>
        {autorizados.fields.map((campo, i) => (
          <div key={campo.id} className="grid gap-2 rounded-md border p-3">
            <div className="grid gap-2 sm:grid-cols-2">
              <CampoTexto
                id={`aut-nome-${i}`}
                rotulo="Nome"
                erro={e.autorizados?.[i]?.nome?.message}
                {...register(`autorizados.${i}.nome`)}
              />
              <CampoTexto
                id={`aut-doc-${i}`}
                rotulo="Documento"
                erro={e.autorizados?.[i]?.documento?.message}
                {...register(`autorizados.${i}.documento`)}
              />
              <CampoTexto
                id={`aut-par-${i}`}
                rotulo="Parentesco"
                erro={e.autorizados?.[i]?.parentesco?.message}
                {...register(`autorizados.${i}.parentesco`)}
              />
              <CampoTexto
                id={`aut-tel-${i}`}
                rotulo="Telefone"
                {...register(`autorizados.${i}.telefone`)}
              />
            </div>
            {autorizados.fields.length > 1 && (
              <Button
                type="button"
                size="sm"
                variant="ghost"
                className="justify-self-end"
                onClick={() => autorizados.remove(i)}
              >
                <Trash2 /> Remover
              </Button>
            )}
          </div>
        ))}
        {e.autorizados?.root?.message && (
          <FieldError>{e.autorizados.root.message}</FieldError>
        )}
        <Button
          type="button"
          size="sm"
          variant="outline"
          onClick={() =>
            autorizados.append({
              nome: "",
              documento: "",
              parentesco: "",
              telefone: "",
            })
          }
        >
          <Plus /> Adicionar autorizado
        </Button>
      </FieldSet>
    </FormularioDialog>
  );
}
