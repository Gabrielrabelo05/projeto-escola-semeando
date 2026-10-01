"use client";

import { Receipt, Send } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { Cobranca } from "@/domain/models/cobranca";
import { GraficoFinanceiro } from "@/presentation/components/features/painel/GraficoFinanceiro";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Kpi } from "@/presentation/components/shared/Kpi";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, mesAtual, moeda, rotulo } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { PagamentoManualDialog } from "./PagamentoManualDialog";

/** Módulo 4 – Cobrança / Cora (RF-23 a RF-30, RF-51). */
export function FinanceiroTela() {
  const { possuiPerfil } = useAuth();
  const gestorFinanceiro = possuiPerfil("ADMIN", "DIRETOR", "FINANCEIRO");
  const [mes, setMes] = useState(mesAtual());

  return (
    <>
      <PageHeader
        titulo="Financeiro"
        descricao="Boletos Cora, pagamentos, inadimplência e relatórios"
        acoes={
          <Input
            type="month"
            aria-label="Mês de referência"
            value={mes}
            onChange={(e) => setMes(e.target.value)}
            className="w-40"
          />
        }
      />
      <Tabs defaultValue="cobrancas">
        <TabsList>
          <TabsTrigger value="cobrancas">Cobranças</TabsTrigger>
          {gestorFinanceiro && (
            <TabsTrigger value="inadimplencia">Inadimplência</TabsTrigger>
          )}
          {gestorFinanceiro && (
            <TabsTrigger value="relatorio">Relatório</TabsTrigger>
          )}
        </TabsList>
        <TabsContent value="cobrancas">
          <Cobrancas mes={mes} podeGerar={gestorFinanceiro} />
        </TabsContent>
        {gestorFinanceiro && (
          <TabsContent value="inadimplencia">
            <Inadimplencia mes={mes} />
          </TabsContent>
        )}
        {gestorFinanceiro && (
          <TabsContent value="relatorio">
            <Relatorio mes={mes} />
          </TabsContent>
        )}
      </Tabs>
    </>
  );
}

function Cobrancas({ mes, podeGerar }: { mes: string; podeGerar: boolean }) {
  const { cobranca } = useGateways();
  const estado = useCarregar(() => cobranca.listar(mes), [mes]);
  const [pagando, setPagando] = useState<Cobranca | null>(null);
  const gerar = useAcao(async () => {
    const resultado = await cobranca.gerarMensal(mes);
    toast.success(`${resultado.geradas.length} boletos emitidos no Cora`, {
      description: resultado.ignoradas.length
        ? `${resultado.ignoradas.length} ignorados (já emitidos ou RN-07)`
        : undefined,
    });
    await estado.recarregar();
  });

  return (
    <div className="space-y-3">
      {podeGerar && (
        <Button onClick={() => gerar.executar()} disabled={gerar.executando}>
          <Send /> Gerar boletos de {mes}
        </Button>
      )}
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhuma cobrança neste mês."
      >
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(c) => c.id}
            colunas={[
              {
                titulo: "Aluno",
                celula: (c) => (
                  <span className="font-medium">{c.aluno?.nome}</span>
                ),
              },
              { titulo: "Tipo", celula: (c) => rotulo(c.tipo) },
              { titulo: "Vencimento", celula: (c) => data(c.vencimento) },
              {
                titulo: "Valor",
                celula: (c) => moeda(c.valor),
                className: "text-right tabular-nums",
              },
              {
                titulo: "Atualizado",
                celula: (c) => moeda(c.valorAtualizado),
                className: "text-right tabular-nums",
              },
              {
                titulo: "Status",
                celula: (c) => (
                  <StatusBadge status={c.vencida ? "VENCIDA" : c.status} />
                ),
              },
              {
                titulo: "Ações",
                celula: (c) =>
                  c.status === "EMITIDA" ? (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => setPagando(c)}
                    >
                      <Receipt /> Baixa manual
                    </Button>
                  ) : (
                    <span className="text-xs text-muted-foreground">
                      {c.pagoEm ? `Pago ${data(c.pagoEm)}` : ""}
                    </span>
                  ),
              },
            ]}
          />
        )}
      </Conteudo>
      <PagamentoManualDialog
        cobranca={pagando}
        aoFechar={() => setPagando(null)}
        aoSalvar={estado.recarregar}
      />
    </div>
  );
}

function Inadimplencia({ mes }: { mes: string }) {
  const { cobranca } = useGateways();
  const [todosMeses, setTodosMeses] = useState(true);
  const estado = useCarregar(
    () => cobranca.inadimplencia(todosMeses ? undefined : mes),
    [mes, todosMeses],
  );
  return (
    <div className="space-y-3">
      <Button
        variant="outline"
        size="sm"
        onClick={() => setTodosMeses((v) => !v)}
      >
        {todosMeses ? `Somente ${mes}` : "Todos os meses em aberto"}
      </Button>
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhum aluno inadimplente."
      >
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(i) => i.cobrancaId}
            colunas={[
              {
                titulo: "Aluno",
                celula: (i) => (
                  <span className="font-medium">{i.aluno.nome}</span>
                ),
              },
              { titulo: "Mês", celula: (i) => i.mesReferencia },
              { titulo: "Dias em atraso", celula: (i) => i.diasAtraso },
              {
                titulo: "Original",
                celula: (i) => moeda(i.valorOriginal),
                className: "text-right tabular-nums",
              },
              {
                titulo: "Multa 2%",
                celula: (i) => moeda(i.multa),
                className: "text-right tabular-nums",
              },
              {
                titulo: "Juros 0,033%/dia",
                celula: (i) => moeda(i.juros),
                className: "text-right tabular-nums",
              },
              {
                titulo: "Total",
                celula: (i) => <strong>{moeda(i.valorAtualizado)}</strong>,
                className: "text-right tabular-nums",
              },
            ]}
          />
        )}
      </Conteudo>
    </div>
  );
}

function Relatorio({ mes }: { mes: string }) {
  const { cobranca } = useGateways();
  const relatorio = useCarregar(() => cobranca.relatorio(mes), [mes]);
  const painel = useCarregar(() => cobranca.painel(mes), [mes]);
  return (
    <Conteudo estado={relatorio}>
      {(r) => (
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Kpi titulo="Previsto" valor={moeda(r.previsto)} icone={Receipt} />
            <Kpi titulo="Recebido" valor={moeda(r.recebido)} icone={Receipt} />
            <Kpi
              titulo="Inadimplência"
              valor={moeda(r.inadimplente)}
              icone={Receipt}
              destaque={r.inadimplente > 0 ? "alerta" : "normal"}
            />
            <Kpi
              titulo="Projeção anual"
              valor={moeda(r.projecaoAnual)}
              icone={Receipt}
            />
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Boletos Cora em {mes} (RF-51)</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-4 text-sm">
              {Object.entries(painel.dados ?? {}).map(([status, total]) => (
                <span key={status} className="flex items-center gap-2">
                  <StatusBadge status={status} /> {total}
                </span>
              ))}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle>Previsto x recebido</CardTitle>
            </CardHeader>
            <CardContent>
              <GraficoFinanceiro relatorio={r} />
            </CardContent>
          </Card>
        </div>
      )}
    </Conteudo>
  );
}
