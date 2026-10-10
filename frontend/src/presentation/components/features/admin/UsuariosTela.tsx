"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { KeyRound, Plus } from "lucide-react";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { ConviteAcesso } from "@/domain/models/auth";
import type { Perfil } from "@/domain/models/comum";
import { FormularioDialog } from "@/presentation/components/features/cadastros/FormularioDialog";
import { Campo, CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { LinkAcessoDialog } from "@/presentation/components/shared/LinkAcessoDialog";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import {
  cpfValido,
  rotulo,
  somenteDigitos,
} from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

const PERFIS: Perfil[] = [
  "ADMIN",
  "DIRETOR",
  "SECRETARIA",
  "FINANCEIRO",
  "PROFESSOR",
  "RESPONSAVEL",
  "PRESTADOR",
];

const schema = z
  .object({
    nome: z.string().trim().min(1, "Obrigatório"),
    email: z.email("E-mail inválido"),
    perfil: z.enum(PERFIS as [Perfil, ...Perfil[]]),
    cpf: z.string(),
  })
  .refine(
    (u) => !["PROFESSOR", "RESPONSAVEL"].includes(u.perfil) || cpfValido(u.cpf),
    {
      path: ["cpf"],
      message:
        "Professores e responsáveis precisam de CPF válido (vínculo com o cadastro)",
    },
  );
type Form = z.infer<typeof schema>;

/**
 * Administração de usuários e perfis (RF-35). O acesso é criado sem senha: o sistema gera um link para a
 * própria pessoa definir a senha, e o mesmo botão serve para redefinir a senha de quem esqueceu.
 */
export function UsuariosTela() {
  const { auth } = useGateways();
  const estado = useCarregar(() => auth.listarUsuarios(), []);
  const [aberto, setAberto] = useState(false);
  const [convite, setConvite] = useState<ConviteAcesso | null>(null);
  const { register, control, handleSubmit, reset, formState } = useForm<Form>({
    resolver: zodResolver(schema),
  });
  const criar = useAcao(async (dados: Form) => {
    const criado = await auth.criarUsuario({
      ...dados,
      cpf: dados.cpf ? somenteDigitos(dados.cpf) : undefined,
    });
    setAberto(false);
    setConvite(criado);
    await estado.recarregar();
  });
  const novoLink = useAcao(async (id: string) => {
    setConvite(await auth.novoLinkAcesso(id));
    await estado.recarregar();
  });
  const inativar = useAcao(async (id: string) => {
    await auth.inativarUsuario(id);
    await estado.recarregar();
  }, "Usuário inativado");

  const e = formState.errors;
  return (
    <>
      <PageHeader
        titulo="Usuários"
        descricao="Perfis de acesso: cada perfil enxerga apenas os módulos permitidos"
        acoes={
          <Button
            onClick={() => {
              reset({
                nome: "",
                email: "",
                perfil: "SECRETARIA",
                cpf: "",
              });
              setAberto(true);
            }}
          >
            <Plus /> Novo usuário
          </Button>
        }
      />
      <Conteudo estado={estado}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(u) => u.id}
            colunas={[
              {
                titulo: "Nome",
                celula: (u) => <span className="font-medium">{u.nome}</span>,
              },
              { titulo: "E-mail", celula: (u) => u.email },
              { titulo: "Perfil", celula: (u) => rotulo(u.perfil) },
              {
                titulo: "Situação",
                celula: (u) =>
                  u.ativo && u.acessoPendente ? (
                    <Badge variant="outline">Aguardando senha</Badge>
                  ) : (
                    <span className="flex items-center gap-1">
                      <StatusBadge status={u.ativo ? "ATIVO" : "INATIVO"} />
                      {u.ativo && u.doisFatoresAtivo && (
                        <Badge
                          variant="outline"
                          title="Verificação em duas etapas ativa"
                        >
                          2FA
                        </Badge>
                      )}
                    </span>
                  ),
              },
              {
                titulo: "",
                celula: (u) =>
                  u.ativo && (
                    <div className="flex justify-end gap-1">
                      <Button
                        size="sm"
                        variant="outline"
                        disabled={novoLink.executando}
                        onClick={() => novoLink.executar(u.id)}
                      >
                        <KeyRound />
                        {u.acessoPendente ? "Reenviar link" : "Redefinir senha"}
                      </Button>
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => inativar.executar(u.id)}
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
        aberto={aberto}
        titulo="Novo usuário"
        aoFechar={() => setAberto(false)}
        aoEnviar={handleSubmit((d) => criar.executar(d))}
        enviando={criar.executando}
      >
        <CampoTexto
          id="usr-nome"
          rotulo="Nome"
          erro={e.nome?.message}
          {...register("nome")}
        />
        <CampoTexto
          id="usr-email"
          rotulo="E-mail"
          type="email"
          erro={e.email?.message}
          {...register("email")}
        />
        <p className="text-xs text-muted-foreground">
          Não é preciso definir senha: ao salvar, o sistema gera um link para a
          própria pessoa criar a senha dela.
        </p>
        <Controller
          control={control}
          name="perfil"
          render={({ field }) => (
            <Campo id="usr-perfil" rotulo="Perfil">
              <Selecao
                id="usr-perfil"
                valor={field.value}
                aoMudar={field.onChange}
                opcoes={PERFIS.map((p) => ({ valor: p, rotulo: rotulo(p) }))}
              />
            </Campo>
          )}
        />
        <CampoTexto
          id="usr-cpf"
          rotulo="CPF"
          descricao="Vincula o usuário ao cadastro de professor ou responsável"
          erro={e.cpf?.message}
          {...register("cpf")}
        />
      </FormularioDialog>
      <LinkAcessoDialog convite={convite} aoFechar={() => setConvite(null)} />
    </>
  );
}
