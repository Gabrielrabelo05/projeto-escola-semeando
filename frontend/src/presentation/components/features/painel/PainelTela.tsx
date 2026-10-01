"use client";

import {
  AlertTriangle,
  CalendarClock,
  ClipboardList,
  GraduationCap,
  HandCoins,
  Wallet,
} from "lucide-react";
import Link from "next/link";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Kpi } from "@/presentation/components/shared/Kpi";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import {
  anoAtual,
  data,
  mesAtual,
  moeda,
} from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { GraficoFinanceiro } from "./GraficoFinanceiro";

export function PainelTela() {
  const { usuario, possuiPerfil } = useAuth();
  const secretaria = possuiPerfil("ADMIN", "DIRETOR", "SECRETARIA");
  const financeiro = possuiPerfil("ADMIN", "DIRETOR", "FINANCEIRO");

  return (
    <>
      <PageHeader
        titulo={`Olá, ${usuario?.nome.split(" ")[0] ?? ""}`}
        descricao="Resumo do dia na escola"
      />
      {secretaria && <ResumoSecretaria />}
      {financeiro && <ResumoFinanceiro />}
      <div className="grid gap-4 lg:grid-cols-2">
        {secretaria && <ProximasObrigacoes />}
        <Calendario />
      </div>
    </>
  );
}

function ResumoSecretaria() {
  const { matricula } = useGateways();
  const relatorio = useCarregar(() => matricula.relatorio(anoAtual()), []);
  const pendentes = useCarregar(
    () => matricula.listar({ status: "AGUARDANDO_ANALISE" }),
    [],
  );
  return (
    <Conteudo estado={relatorio}>
      {(r) => {
        const vagas = r.turmas.reduce((t, o) => t + o.vagas, 0);
        return (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Kpi
              titulo="Alunos ativos"
              valor={r.porStatus.ATIVO ?? 0}
              icone={GraduationCap}
            />
            <Kpi
              titulo="Aguardando análise"
              valor={pendentes.dados?.length ?? "…"}
              detalhe="pré-matrículas"
              icone={ClipboardList}
              destaque={
                (pendentes.dados?.length ?? 0) > 0 ? "alerta" : "normal"
              }
            />
            <Kpi
              titulo="Educação Infantil"
              valor={r.porNivel.EDUCACAO_INFANTIL ?? 0}
              detalhe="vagas ocupadas"
              icone={GraduationCap}
            />
            <Kpi
              titulo="Vagas livres"
              valor={vagas}
              detalhe={`em ${r.turmas.length} turmas`}
              icone={ClipboardList}
            />
          </div>
        );
      }}
    </Conteudo>
  );
}

function ResumoFinanceiro() {
  const { cobranca } = useGateways();
  const relatorio = useCarregar(() => cobranca.relatorio(mesAtual()), []);
  return (
    <Conteudo estado={relatorio}>
      {(r) => (
        <div className="space-y-4">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Kpi
              titulo="Previsto no mês"
              valor={moeda(r.previsto)}
              icone={Wallet}
            />
            <Kpi
              titulo="Recebido no mês"
              valor={moeda(r.recebido)}
              icone={HandCoins}
            />
            <Kpi
              titulo="Inadimplente"
              valor={moeda(r.inadimplente)}
              icone={AlertTriangle}
              destaque={r.inadimplente > 0 ? "alerta" : "normal"}
            />
            <Kpi
              titulo="Projeção anual"
              valor={moeda(r.projecaoAnual)}
              icone={Wallet}
            />
          </div>
          <Card>
            <CardHeader>
              <CardTitle>Receita {r.mes.slice(0, 4)}</CardTitle>
              <CardDescription>Previsto x recebido por mês</CardDescription>
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

function ProximasObrigacoes() {
  const { obrigacao } = useGateways();
  const estado = useCarregar(() => obrigacao.listar({ ano: anoAtual() }), []);
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <CalendarClock className="size-4" /> Obrigações regulatórias
        </CardTitle>
        <CardDescription>
          SMTT, Censo Escolar e Sistema Presença
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Conteudo
          estado={estado}
          vazio={(l) => l.length === 0}
          mensagemVazio="Nenhuma obrigação cadastrada."
        >
          {(lista) => (
            <ul className="divide-y">
              {lista
                .filter((o) => o.status !== "CONCLUIDA")
                .slice(0, 5)
                .map((o) => (
                  <li
                    key={o.id}
                    className="flex items-center justify-between gap-2 py-2 text-sm"
                  >
                    <Link href="/obrigacoes" className="hover:underline">
                      {o.nome}
                    </Link>
                    <span
                      className={`shrink-0 whitespace-nowrap ${
                        o.atrasada
                          ? "font-medium text-destructive"
                          : "text-muted-foreground"
                      }`}
                    >
                      {o.atrasada ? "Atrasada" : `${o.diasRestantes} dias`} ·{" "}
                      {data(o.prazo)}
                    </span>
                  </li>
                ))}
            </ul>
          )}
        </Conteudo>
      </CardContent>
    </Card>
  );
}

function Calendario() {
  const { pedagogico } = useGateways();
  const estado = useCarregar(() => pedagogico.calendario(anoAtual()), []);
  const hoje = new Date().toISOString().slice(0, 10);
  return (
    <Card>
      <CardHeader>
        <CardTitle>Calendário letivo</CardTitle>
        <CardDescription>Próximos eventos</CardDescription>
      </CardHeader>
      <CardContent>
        <Conteudo
          estado={estado}
          vazio={(l) => l.length === 0}
          mensagemVazio="Nenhum evento publicado."
        >
          {(eventos) => (
            <ul className="divide-y">
              {eventos
                .filter((e) => e.fim >= hoje)
                .slice(0, 5)
                .map((e) => (
                  <li
                    key={e.id}
                    className="flex items-center justify-between gap-2 py-2 text-sm"
                  >
                    <span>{e.titulo}</span>
                    <span className="flex items-center gap-2 text-muted-foreground">
                      <StatusBadge status={e.tipo} /> {data(e.inicio)}
                    </span>
                  </li>
                ))}
            </ul>
          )}
        </Conteudo>
      </CardContent>
    </Card>
  );
}
