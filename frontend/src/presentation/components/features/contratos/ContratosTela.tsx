"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import type { Contrato, StatusContrato } from "@/domain/models/contrato";
import { mensagemDeErro } from "@/domain/models/erro";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { anoAtual, data, moeda, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { ContratoVisualizacao } from "./ContratoVisualizacao";

const TODAS = "TODAS";

/** Módulo 2 – Contratos (RF-13 a RF-18). */
export function ContratosTela() {
  const { contrato, cadastro, matricula } = useGateways();
  const [turmaId, setTurmaId] = useState<string>(TODAS);
  const [status, setStatus] = useState<string>(TODAS);
  const [aberto, setAberto] = useState<Contrato | null>(null);
  const turmas = useCarregar(() => cadastro.turmas(anoAtual()), []);
  const estado = useCarregar(
    () =>
      contrato.pesquisar({
        anoLetivo: anoAtual(),
        turmaId: turmaId === TODAS ? undefined : turmaId,
        status: status === TODAS ? undefined : (status as StatusContrato),
      }),
    [turmaId, status],
  );
  const novaVersao = useAcao(async (c: Contrato) => {
    const nova = await contrato.gerarNovaVersao(c.id);
    setAberto(nova);
    await estado.recarregar();
  }, "Nova versão gerada – aguardando assinatura");

  return (
    <>
      <PageHeader
        titulo="Contratos e fichas de sócio"
        descricao="Repositório digital com versionamento e assinatura eletrônica"
      />
      <div className="flex flex-wrap gap-2">
        <Selecao
          valor={turmaId}
          aoMudar={setTurmaId}
          className="w-48"
          opcoes={[
            { valor: TODAS, rotulo: "Todas as turmas" },
            ...(turmas.dados ?? []).map((t) => ({
              valor: t.id,
              rotulo: t.nome,
            })),
          ]}
        />
        <Selecao
          valor={status}
          aoMudar={setStatus}
          className="w-56"
          opcoes={[
            TODAS,
            "AGUARDANDO_ASSINATURA",
            "ASSINADO",
            "SUBSTITUIDO",
          ].map((s) => ({
            valor: s,
            rotulo: s === TODAS ? "Todos os status" : rotulo(s),
          }))}
        />
      </div>
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(c) => c.id}
            aoClicar={setAberto}
            colunas={[
              { titulo: "Documento", celula: (c) => rotulo(c.tipo) },
              { titulo: "Versão", celula: (c) => `v${c.versao}` },
              {
                titulo: "Valor",
                celula: (c) =>
                  c.valorTotal !== null
                    ? moeda(c.valorTotal)
                    : moeda(c.valorTaxaAssociado),
              },
              { titulo: "Gerado em", celula: (c) => data(c.geradoEm) },
              {
                titulo: "Status",
                celula: (c) => <StatusBadge status={c.status} />,
              },
            ]}
          />
        )}
      </Conteudo>
      <ValidacaoValores
        aoGerar={async (matriculaId, valor, parcelas) => {
          try {
            const gerado = await contrato.gerar(matriculaId, valor, parcelas);
            toast.success("Contrato gerado");
            setAberto(gerado);
            await estado.recarregar();
          } catch (e) {
            toast.error(mensagemDeErro(e));
          }
        }}
        matriculas={() => matricula.listar({ status: "APROVADA" })}
      />
      <ContratoVisualizacao
        contrato={aberto}
        aoFechar={() => setAberto(null)}
        acoesExtras={
          aberto && aberto.status !== "SUBSTITUIDO" ? (
            <Button
              variant="outline"
              disabled={novaVersao.executando}
              onClick={() => novaVersao.executar(aberto)}
            >
              Gerar nova versão
            </Button>
          ) : null
        }
      />
    </>
  );
}

interface ValidacaoProps {
  aoGerar: (
    matriculaId: string,
    valor: number,
    parcelas: number,
  ) => Promise<void>;
  matriculas: () => ReturnType<
    ReturnType<typeof useGateways>["matricula"]["listar"]
  >;
}

/** RF-17: a geração com valores informados é bloqueada se divergir da tabela oficial (RN-02). */
function ValidacaoValores({ aoGerar, matriculas }: ValidacaoProps) {
  const lista = useCarregar(matriculas, []);
  const [matriculaId, setMatriculaId] = useState<string>();
  const [valor, setValor] = useState("375.00");
  const [parcelas, setParcelas] = useState("11");
  return (
    <Card size="sm">
      <CardHeader>
        <CardTitle>Gerar contrato com valores informados</CardTitle>
        <CardDescription>
          Tabela oficial do Ensino Fundamental: 11 parcelas de R$ 375,00 (total
          R$ 4.125,00).
        </CardDescription>
      </CardHeader>
      <CardContent className="flex flex-wrap items-end gap-2">
        <Selecao
          valor={matriculaId}
          aoMudar={setMatriculaId}
          className="w-64"
          placeholder="Matrícula aprovada"
          opcoes={(lista.dados ?? []).map((m) => ({
            valor: m.id,
            rotulo: `${m.aluno?.nome} – ${m.turma?.nome}`,
          }))}
        />
        <Input
          aria-label="Valor da parcela"
          className="w-28"
          value={valor}
          onChange={(e) => setValor(e.target.value)}
        />
        <Input
          aria-label="Número de parcelas"
          className="w-20"
          value={parcelas}
          onChange={(e) => setParcelas(e.target.value)}
        />
        <Button
          disabled={!matriculaId}
          onClick={() =>
            aoGerar(matriculaId as string, Number(valor), Number(parcelas))
          }
        >
          Validar e gerar
        </Button>
      </CardContent>
    </Card>
  );
}
