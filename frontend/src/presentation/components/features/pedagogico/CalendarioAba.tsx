"use client";

import { Trash2 } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import type { TipoEvento } from "@/domain/models/pedagogico";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import {
  anoAtual,
  data,
  hojeISO,
  rotulo,
} from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";

const TIPOS: TipoEvento[] = [
  "EVENTO",
  "FERIADO",
  "REUNIAO_PAIS",
  "FIM_BIMESTRE",
  "INICIO_ANO_LETIVO",
  "FIM_ANO_LETIVO",
];

/** RF-42: calendário letivo visível a todos os perfis. */
export function CalendarioAba() {
  const { pedagogico } = useGateways();
  const { possuiPerfil } = useAuth();
  const podeEditar = possuiPerfil("ADMIN", "DIRETOR", "SECRETARIA");
  const estado = useCarregar(() => pedagogico.calendario(anoAtual()), []);
  const [titulo, setTitulo] = useState("");
  const [tipo, setTipo] = useState<TipoEvento>("EVENTO");
  const [inicio, setInicio] = useState(hojeISO());
  const publicar = useAcao(async () => {
    await pedagogico.publicarEvento({ titulo, tipo, inicio, fim: inicio });
    setTitulo("");
    await estado.recarregar();
  }, "Evento publicado");
  const remover = useAcao(async (id: string) => {
    await pedagogico.removerEvento(id);
    await estado.recarregar();
  }, "Evento removido");

  return (
    <div className="space-y-4">
      {podeEditar && (
        <div className="flex flex-wrap items-end gap-2">
          <CampoTexto
            id="evento-titulo"
            rotulo="Evento"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
          />
          <CampoTexto
            id="evento-data"
            rotulo="Data"
            type="date"
            value={inicio}
            onChange={(e) => setInicio(e.target.value)}
          />
          <Selecao
            valor={tipo}
            aoMudar={(v) => setTipo(v as TipoEvento)}
            className="w-44"
            opcoes={TIPOS.map((t) => ({ valor: t, rotulo: rotulo(t) }))}
          />
          <Button disabled={!titulo.trim()} onClick={() => publicar.executar()}>
            Publicar
          </Button>
        </div>
      )}
      <Conteudo estado={estado} vazio={(l) => l.length === 0}>
        {(lista) => (
          <Tabela
            linhas={lista}
            chave={(e) => e.id}
            colunas={[
              {
                titulo: "Data",
                celula: (e) =>
                  e.inicio === e.fim
                    ? data(e.inicio)
                    : `${data(e.inicio)} – ${data(e.fim)}`,
              },
              { titulo: "Evento", celula: (e) => e.titulo },
              {
                titulo: "Tipo",
                celula: (e) => <StatusBadge status={e.tipo} />,
              },
              {
                titulo: "",
                celula: (e) =>
                  podeEditar && (
                    <Button
                      size="icon-sm"
                      variant="ghost"
                      aria-label={`Remover ${e.titulo}`}
                      onClick={() => remover.executar(e.id)}
                    >
                      <Trash2 />
                    </Button>
                  ),
              },
            ]}
          />
        )}
      </Conteudo>
    </div>
  );
}
