"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { CheckCircle2, Plus, Trash2 } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { Controller, useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import {
  FieldError,
  FieldGroup,
  FieldLegend,
  FieldSet,
} from "@/components/ui/field";
import { Label } from "@/components/ui/label";
import { mensagemDeErro } from "@/domain/models/erro";
import type { Matricula } from "@/domain/models/matricula";
import { Marca } from "@/presentation/components/layout/Marca";
import { Campo, CampoTexto } from "@/presentation/components/shared/Campo";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { formatarCpf, somenteDigitos } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import {
  ETAPAS,
  type PreMatriculaForm,
  preMatriculaSchema,
} from "@/presentation/schemas/preMatricula";

const VAZIO: PreMatriculaForm = {
  aluno: {
    nome: "",
    dataNascimento: "",
    alergias: "",
    tipoSanguineo: "",
    necessidadesEspeciais: "",
    hospitalEmergencia: "",
    autorizados: [{ nome: "", documento: "", parentesco: "", telefone: "" }],
  },
  responsavel: { nome: "", cpf: "", email: "", telefone: "", parentesco: "" },
  turmaId: "",
  vindoDeEscolaParticular: false,
};

/** RF-01: formulário digital de pré-matrícula, em etapas. */
export function PreMatriculaTela() {
  const { matricula } = useGateways();
  const turmas = useCarregar(() => matricula.turmasAbertas(), []);
  const [etapa, setEtapa] = useState(0);
  const [enviada, setEnviada] = useState<Matricula | null>(null);
  const form = useForm<PreMatriculaForm>({
    resolver: zodResolver(preMatriculaSchema),
    defaultValues: VAZIO,
  });
  const { register, control, handleSubmit, trigger, formState } = form;
  const { errors, isSubmitting } = formState;
  const autorizados = useFieldArray({ control, name: "aluno.autorizados" });

  async function avancar() {
    const valido = await trigger(
      ETAPAS[etapa].campos as unknown as (keyof PreMatriculaForm)[],
    );
    if (valido) setEtapa((e) => e + 1);
  }

  async function enviar(dados: PreMatriculaForm) {
    try {
      const criada = await matricula.solicitarPreMatricula({
        ...dados,
        responsavel: {
          ...dados.responsavel,
          cpf: somenteDigitos(dados.responsavel.cpf),
        },
      });
      setEnviada(criada);
    } catch (e) {
      toast.error(mensagemDeErro(e));
    }
  }

  if (enviada) {
    return (
      <Moldura>
        <CardContent className="flex flex-col items-center gap-3 py-8 text-center">
          <CheckCircle2 className="size-10 text-primary" />
          <h2 className="text-lg font-semibold">Pré-matrícula enviada!</h2>
          <p className="text-sm text-muted-foreground">
            A secretaria vai analisar a ficha e os documentos. Você receberá a
            resposta por e-mail/WhatsApp.
          </p>
          <p className="text-xs text-muted-foreground">
            Protocolo: {enviada.id}
          </p>
          <Link
            href="/login"
            className="text-sm font-medium underline-offset-4 hover:underline"
          >
            Ir para o login
          </Link>
        </CardContent>
      </Moldura>
    );
  }

  const opcoesTurma = (turmas.dados ?? []).map((t) => ({
    valor: t.id,
    rotulo: `${t.nome} (${t.anoLetivo})`,
  }));

  return (
    <Moldura>
      <form onSubmit={handleSubmit(enviar)} noValidate>
        <CardContent>
          <ol className="mb-6 grid grid-cols-4 gap-2" aria-label="Etapas">
            {ETAPAS.map((e, i) => (
              <li
                key={e.titulo}
                aria-current={i === etapa ? "step" : undefined}
                className={`border-t-2 pt-2 text-xs ${i <= etapa ? "border-primary font-medium" : "border-muted text-muted-foreground"}`}
              >
                {e.titulo}
              </li>
            ))}
          </ol>

          {etapa === 0 && (
            <FieldGroup>
              <CampoTexto
                id="aluno-nome"
                rotulo="Nome completo do aluno"
                erro={errors.aluno?.nome?.message}
                {...register("aluno.nome")}
              />
              <CampoTexto
                id="aluno-nascimento"
                rotulo="Data de nascimento"
                type="date"
                erro={errors.aluno?.dataNascimento?.message}
                {...register("aluno.dataNascimento")}
              />
            </FieldGroup>
          )}

          {etapa === 1 && (
            <FieldGroup>
              <div className="grid gap-4 sm:grid-cols-2">
                <CampoTexto
                  id="alergias"
                  rotulo="Alergias"
                  {...register("aluno.alergias")}
                />
                <CampoTexto
                  id="tipo-sanguineo"
                  rotulo="Tipo sanguíneo"
                  {...register("aluno.tipoSanguineo")}
                />
              </div>
              <CampoTexto
                id="necessidades"
                rotulo="Necessidades especiais"
                {...register("aluno.necessidadesEspeciais")}
              />
              <CampoTexto
                id="hospital"
                rotulo="Hospital de emergência"
                descricao="Obrigatório por cláusula contratual."
                erro={errors.aluno?.hospitalEmergencia?.message}
                {...register("aluno.hospitalEmergencia")}
              />
              <FieldSet>
                <FieldLegend>
                  Pessoas autorizadas a buscar a criança
                </FieldLegend>
                {autorizados.fields.map((campo, i) => (
                  <div
                    key={campo.id}
                    className="grid gap-2 rounded-lg border p-3 sm:grid-cols-[1fr_1fr_1fr_auto]"
                  >
                    <CampoTexto
                      id={`aut-nome-${i}`}
                      rotulo="Nome"
                      erro={errors.aluno?.autorizados?.[i]?.nome?.message}
                      {...register(`aluno.autorizados.${i}.nome`)}
                    />
                    <CampoTexto
                      id={`aut-doc-${i}`}
                      rotulo="Documento"
                      erro={errors.aluno?.autorizados?.[i]?.documento?.message}
                      {...register(`aluno.autorizados.${i}.documento`)}
                    />
                    <CampoTexto
                      id={`aut-par-${i}`}
                      rotulo="Parentesco"
                      erro={errors.aluno?.autorizados?.[i]?.parentesco?.message}
                      {...register(`aluno.autorizados.${i}.parentesco`)}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="self-end"
                      aria-label="Remover autorizado"
                      onClick={() => autorizados.remove(i)}
                    >
                      <Trash2 />
                    </Button>
                  </div>
                ))}
                {errors.aluno?.autorizados?.root && (
                  <FieldError>
                    {errors.aluno.autorizados.root.message}
                  </FieldError>
                )}
                {errors.aluno?.autorizados?.message && (
                  <FieldError>{errors.aluno.autorizados.message}</FieldError>
                )}
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="w-fit"
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
            </FieldGroup>
          )}

          {etapa === 2 && (
            <FieldGroup>
              <CampoTexto
                id="resp-nome"
                rotulo="Nome do responsável"
                erro={errors.responsavel?.nome?.message}
                {...register("responsavel.nome")}
              />
              <div className="grid gap-4 sm:grid-cols-2">
                <Controller
                  control={control}
                  name="responsavel.cpf"
                  render={({ field }) => (
                    <CampoTexto
                      id="resp-cpf"
                      rotulo="CPF"
                      inputMode="numeric"
                      erro={errors.responsavel?.cpf?.message}
                      value={field.value}
                      onBlur={field.onBlur}
                      onChange={(e) =>
                        field.onChange(formatarCpf(e.target.value))
                      }
                    />
                  )}
                />
                <CampoTexto
                  id="resp-parentesco"
                  rotulo="Parentesco"
                  erro={errors.responsavel?.parentesco?.message}
                  {...register("responsavel.parentesco")}
                />
                <CampoTexto
                  id="resp-email"
                  rotulo="E-mail"
                  type="email"
                  erro={errors.responsavel?.email?.message}
                  {...register("responsavel.email")}
                />
                <CampoTexto
                  id="resp-telefone"
                  rotulo="Telefone (WhatsApp)"
                  erro={errors.responsavel?.telefone?.message}
                  {...register("responsavel.telefone")}
                />
              </div>
            </FieldGroup>
          )}

          {etapa === 3 && (
            <FieldGroup>
              <Controller
                control={control}
                name="turmaId"
                render={({ field }) => (
                  <Campo
                    id="turma"
                    rotulo="Turma pretendida"
                    erro={errors.turmaId?.message}
                  >
                    <Selecao
                      id="turma"
                      valor={field.value}
                      opcoes={opcoesTurma}
                      aoMudar={field.onChange}
                      invalido={!!errors.turmaId}
                    />
                  </Campo>
                )}
              />
              <Controller
                control={control}
                name="vindoDeEscolaParticular"
                render={({ field }) => (
                  <div className="flex items-start gap-2">
                    <Checkbox
                      id="escola-particular"
                      checked={field.value}
                      onCheckedChange={(v) => field.onChange(v === true)}
                    />
                    <Label htmlFor="escola-particular" className="font-normal">
                      O aluno vem de outra escola particular (será pedida a
                      declaração de adimplência)
                    </Label>
                  </div>
                )}
              />
              <p className="text-xs text-muted-foreground">
                Os dados são tratados conforme a LGPD (Lei 13.709/2018, Art. 14)
                e usados exclusivamente para a matrícula.
              </p>
            </FieldGroup>
          )}
        </CardContent>
        <CardFooter className="mt-6 justify-between">
          <Button
            type="button"
            variant="outline"
            disabled={etapa === 0}
            onClick={() => setEtapa((e) => e - 1)}
          >
            Voltar
          </Button>
          {etapa < ETAPAS.length - 1 ? (
            <Button type="button" onClick={avancar}>
              Continuar
            </Button>
          ) : (
            <Button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Enviando…" : "Enviar pré-matrícula"}
            </Button>
          )}
        </CardFooter>
      </form>
    </Moldura>
  );
}

function Moldura({ children }: { children: React.ReactNode }) {
  return (
    <main className="flex min-h-screen items-start justify-center bg-muted/40 p-4 sm:items-center">
      <Card className="w-full max-w-2xl">
        <CardHeader>
          <Marca />
          <CardTitle className="pt-4 text-lg">Pré-matrícula</CardTitle>
          <CardDescription>
            Centro Educacional Semeando · Maternal I ao 5º ano
          </CardDescription>
        </CardHeader>
        {children}
      </Card>
    </main>
  );
}
