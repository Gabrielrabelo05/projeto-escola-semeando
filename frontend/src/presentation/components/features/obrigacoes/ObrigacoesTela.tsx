"use client";

import { CalendarPlus, CheckCircle2, Download, FileUp } from "lucide-react";
import { useRef, useState } from "react";
import { Badge } from "@/components/ui/badge";
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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import type { Obrigacao } from "@/domain/models/obrigacao";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import {
  anoAtual,
  baixarArquivo,
  data,
  dataHora,
} from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";

/** Módulo 7 – Obrigações regulatórias (RF-47 a RF-54, RN-10). */
export function ObrigacoesTela() {
  const { obrigacao } = useGateways();
  const { possuiPerfil } = useAuth();
  const gestao = possuiPerfil("ADMIN", "DIRETOR", "SECRETARIA");
  const [ano, setAno] = useState(anoAtual());
  const estado = useCarregar(() => obrigacao.listar({ ano }), [ano]);
  const gerarCalendario = useAcao(async () => {
    await obrigacao.gerarCalendarioAnual(ano, "Secretaria Escolar");
    await estado.recarregar();
  }, "Calendário regulatório gerado");
  const censo = useAcao(async () =>
    baixarArquivo(await obrigacao.exportarCenso(ano)),
  );
  const alertas = useAcao(
    async () => obrigacao.verificarAlertas(),
    "Alertas verificados",
  );

  return (
    <>
      <PageHeader
        titulo="Obrigações regulatórias"
        descricao="SMTT (31/03), Censo Escolar (30/06) e Sistema Presença (fim de cada bimestre) – alertas com 30 e 15 dias"
        acoes={
          <>
            <Input
              type="number"
              aria-label="Ano"
              className="w-24"
              value={ano}
              onChange={(e) => setAno(Number(e.target.value))}
            />
            {gestao && (
              <>
                <Button
                  variant="outline"
                  onClick={() => gerarCalendario.executar()}
                >
                  <CalendarPlus /> Gerar calendário do ano
                </Button>
                <Button variant="outline" onClick={() => censo.executar()}>
                  <Download /> Exportar Censo
                </Button>
              </>
            )}
            {possuiPerfil("ADMIN", "DIRETOR") && (
              <Button variant="ghost" onClick={() => alertas.executar()}>
                Verificar alertas
              </Button>
            )}
          </>
        }
      />
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhuma obrigação cadastrada para o ano."
      >
        {(lista) => (
          <div className="grid gap-4 lg:grid-cols-2">
            {lista.map((o) => (
              <CartaoObrigacao
                key={o.id}
                obrigacao={o}
                gestao={gestao}
                aoAlterar={estado.recarregar}
              />
            ))}
          </div>
        )}
      </Conteudo>
    </>
  );
}

function CartaoObrigacao({
  obrigacao: o,
  gestao,
  aoAlterar,
}: {
  obrigacao: Obrigacao;
  gestao: boolean;
  aoAlterar: () => void;
}) {
  const { obrigacao } = useGateways();
  const arquivo = useRef<HTMLInputElement>(null);
  const concluida = o.status === "CONCLUIDA";
  const acao = useAcao(async (operacao: Promise<unknown>) => {
    await operacao;
    aoAlterar();
  });
  const baixar = useAcao(async (i: number) =>
    baixarArquivo(await obrigacao.baixarComprovante(o.id, i)),
  );

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex flex-wrap items-center gap-2">
          {o.nome} <StatusBadge status={o.status} />
        </CardTitle>
        <CardDescription>
          Prazo {data(o.prazo)} · {o.responsavel} ·{" "}
          {concluida ? (
            `concluída em ${data(o.concluidaEm)}`
          ) : o.atrasada ? (
            <Badge variant="destructive">Atrasada</Badge>
          ) : (
            <span
              className={
                o.diasRestantes <= 15 ? "font-medium text-destructive" : ""
              }
            >
              {o.diasRestantes} dias restantes
            </span>
          )}
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-3">
        {o.checklist.length > 0 && (
          <ul className="space-y-1.5">
            {o.checklist.map((item, i) => (
              <li key={item.descricao} className="flex items-center gap-2">
                <Checkbox
                  id={`${o.id}-${i}`}
                  checked={item.concluido}
                  disabled={concluida || !gestao}
                  onCheckedChange={(v) =>
                    acao.executar(obrigacao.marcarItem(o.id, i, v === true))
                  }
                />
                <Label htmlFor={`${o.id}-${i}`} className="font-normal">
                  {item.descricao}
                </Label>
              </li>
            ))}
          </ul>
        )}
        {o.comprovantes.length > 0 && (
          <ul className="space-y-1 text-xs text-muted-foreground">
            {o.comprovantes.map((c, i) => (
              <li key={`${c.nomeArquivo}-${c.enviadoEm}`}>
                <button
                  type="button"
                  className="underline"
                  onClick={() => baixar.executar(i)}
                >
                  {c.nomeArquivo}
                </button>{" "}
                · {c.enviadoPor} · {dataHora(c.enviadoEm)}
              </li>
            ))}
          </ul>
        )}
      </CardContent>
      {!concluida && (
        <CardFooter className="gap-2">
          <Button
            size="sm"
            variant="outline"
            onClick={() => arquivo.current?.click()}
          >
            <FileUp /> Anexar comprovante
          </Button>
          {gestao && (
            <Button
              size="sm"
              disabled={o.comprovantes.length === 0}
              onClick={() => acao.executar(obrigacao.concluir(o.id))}
            >
              <CheckCircle2 /> Concluir
            </Button>
          )}
          <input
            ref={arquivo}
            type="file"
            hidden
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file)
                void acao.executar(obrigacao.anexarComprovante(o.id, file));
              e.target.value = "";
            }}
          />
        </CardFooter>
      )}
    </Card>
  );
}
