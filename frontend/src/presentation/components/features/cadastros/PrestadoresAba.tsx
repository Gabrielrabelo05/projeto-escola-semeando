"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Plus } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import type { z } from "zod";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { Prestador } from "@/domain/models/cadastro";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { prestadorSchema } from "@/presentation/schemas/cadastros";
import { FormularioDialog } from "./FormularioDialog";

type Form = z.input<typeof prestadorSchema>;

/** RF-34 */
export function PrestadoresAba() {
  const { cadastro } = useGateways();
  const estado = useCarregar(() => cadastro.prestadores(true), []);
  const [editando, setEditando] = useState<Prestador | "novo" | null>(null);
  const { register, handleSubmit, reset, formState } = useForm<
    Form,
    unknown,
    z.output<typeof prestadorSchema>
  >({
    resolver: zodResolver(prestadorSchema),
  });
  const salvar = useAcao(async (dados: z.output<typeof prestadorSchema>) => {
    await cadastro.salvarPrestador(
      dados,
      editando !== "novo" ? editando?.id : undefined,
    );
    setEditando(null);
    await estado.recarregar();
  }, "Prestador salvo");

  function abrir(p: Prestador | "novo") {
    reset(
      p === "novo"
        ? {
            nome: "",
            documento: "",
            servicoPrestado: "",
            vigenciaInicio: "",
            vigenciaFim: "",
            documentosObrigatorios: "",
            email: "",
            telefone: "",
          }
        : {
            ...p,
            documentosObrigatorios: p.documentosObrigatorios.join(", "),
            email: p.email ?? "",
            telefone: p.telefone ?? "",
          },
    );
    setEditando(p);
  }

  const e = formState.errors;
  return (
    <div className="space-y-3">
      <Button onClick={() => abrir("novo")}>
        <Plus /> Novo prestador
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
              { titulo: "Serviço", celula: (p) => p.servicoPrestado },
              {
                titulo: "Vigência",
                celula: (p) =>
                  `${data(p.vigenciaInicio)} a ${data(p.vigenciaFim)}`,
              },
              {
                titulo: "Contrato",
                celula: (p) =>
                  p.contratoVigente ? (
                    <Badge>Vigente</Badge>
                  ) : (
                    <Badge variant="destructive">Vencido</Badge>
                  ),
              },
            ]}
          />
        )}
      </Conteudo>
      <FormularioDialog
        aberto={editando !== null}
        titulo={editando === "novo" ? "Novo prestador" : "Editar prestador"}
        aoFechar={() => setEditando(null)}
        aoEnviar={handleSubmit((d) => salvar.executar(d))}
        enviando={salvar.executando}
      >
        <CampoTexto
          id="prest-nome"
          rotulo="Nome / razão social"
          erro={e.nome?.message}
          {...register("nome")}
        />
        <div className="grid gap-4 sm:grid-cols-2">
          <CampoTexto
            id="prest-doc"
            rotulo="CPF ou CNPJ"
            disabled={editando !== "novo"}
            erro={e.documento?.message}
            {...register("documento")}
          />
          <CampoTexto
            id="prest-servico"
            rotulo="Serviço prestado"
            erro={e.servicoPrestado?.message}
            {...register("servicoPrestado")}
          />
          <CampoTexto
            id="prest-ini"
            rotulo="Início da vigência"
            type="date"
            erro={e.vigenciaInicio?.message}
            {...register("vigenciaInicio")}
          />
          <CampoTexto
            id="prest-fim"
            rotulo="Fim da vigência"
            type="date"
            erro={e.vigenciaFim?.message}
            {...register("vigenciaFim")}
          />
          <CampoTexto
            id="prest-email"
            rotulo="E-mail"
            erro={e.email?.message}
            {...register("email")}
          />
          <CampoTexto
            id="prest-tel"
            rotulo="Telefone"
            {...register("telefone")}
          />
        </div>
        <CampoTexto
          id="prest-docs"
          rotulo="Documentos obrigatórios"
          descricao="Ex.: CNPJ, alvará, seguro – separados por vírgula"
          {...register("documentosObrigatorios")}
        />
      </FormularioDialog>
    </div>
  );
}
