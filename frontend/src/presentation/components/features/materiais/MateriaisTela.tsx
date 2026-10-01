"use client";

import { Download, Plus, Trash2 } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { SERIES, type Serie } from "@/domain/models/comum";
import type {
  ItemMaterial,
  ListaMateriais,
  TipoItem,
} from "@/domain/models/material";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { anoAtual, baixarArquivo, data } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

type ItemEditavel = ItemMaterial & { uid: string };

const novoItem = (): ItemEditavel => ({
  uid: crypto.randomUUID(),
  descricao: "",
  quantidade: 1,
  tipo: "MATERIAL",
  linkCompra: "",
});

/** Módulo 3 – Listas de materiais e livros por série (RF-05, RF-19 a RF-22). */
export function MateriaisTela() {
  const { material } = useGateways();
  const [serie, setSerie] = useState<Serie>("PRIMEIRO_ANO");
  const estado = useCarregar(() => material.listar(serie), [serie]);
  const [itens, setItens] = useState<ItemEditavel[]>(() => [novoItem()]);

  const criar = useAcao(async () => {
    await material.criar(
      serie,
      anoAtual() + 1,
      itens
        .filter((i) => i.descricao.trim())
        .map(({ uid: _uid, ...item }) => item),
    );
    setItens([novoItem()]);
    await estado.recarregar();
  }, "Lista criada como rascunho");
  const publicar = useAcao(async (lista: ListaMateriais) => {
    await material.publicar(
      lista.id,
      `${lista.anoLetivo}-01-01`,
      `${lista.anoLetivo}-12-31`,
    );
    await estado.recarregar();
  }, "Lista publicada – responsáveis notificados");
  const baixar = useAcao(async () =>
    baixarArquivo(await material.baixarPdf(serie)),
  );

  const atualizarItem = (i: number, parcial: Partial<ItemMaterial>) =>
    setItens((lista) =>
      lista.map((item, j) => (j === i ? { ...item, ...parcial } : item)),
    );

  return (
    <>
      <PageHeader
        titulo="Listas de materiais"
        descricao="Publicação com vigência; listas anteriores ficam arquivadas"
        acoes={
          <Button variant="outline" onClick={() => baixar.executar()}>
            <Download /> PDF da lista publicada
          </Button>
        }
      />
      <Selecao
        valor={serie}
        aoMudar={(v) => setSerie(v as Serie)}
        className="w-48"
        opcoes={SERIES.map((s) => ({ valor: s.valor, rotulo: s.descricao }))}
      />
      <Conteudo
        estado={estado}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhuma lista para esta série."
      >
        {(listas) => (
          <div className="grid gap-4 md:grid-cols-2">
            {listas.map((lista) => (
              <Card key={lista.id}>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    {lista.serieDescricao} · {lista.anoLetivo}{" "}
                    <StatusBadge status={lista.status} />
                  </CardTitle>
                  <CardDescription>
                    {lista.vigenciaInicio
                      ? `Vigência ${data(lista.vigenciaInicio)} a ${data(lista.vigenciaFim)}`
                      : "Ainda não publicada"}
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-3">
                  <ul className="space-y-1 text-sm">
                    {lista.itens.map((item) => (
                      <li
                        key={item.descricao}
                        className="flex justify-between gap-2"
                      >
                        <span>
                          {item.quantidade}× {item.descricao}
                          {item.linkCompra && (
                            <a
                              href={item.linkCompra}
                              target="_blank"
                              rel="noreferrer"
                              className="ml-1 text-xs underline"
                            >
                              comprar
                            </a>
                          )}
                        </span>
                        <span className="text-xs text-muted-foreground">
                          {item.tipo === "LIVRO" ? "Livro" : "Material"}
                        </span>
                      </li>
                    ))}
                  </ul>
                  {lista.status === "RASCUNHO" && (
                    <Button size="sm" onClick={() => publicar.executar(lista)}>
                      Publicar para {lista.anoLetivo}
                    </Button>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </Conteudo>

      <Card>
        <CardHeader>
          <CardTitle>Nova lista para {anoAtual() + 1}</CardTitle>
          <CardDescription>
            Livros podem ter link para loja externa (somente leitura no portal).
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-2">
          {itens.map((item, i) => (
            <div
              key={item.uid}
              className="grid gap-2 sm:grid-cols-[2fr_80px_140px_2fr_auto]"
            >
              <Input
                aria-label="Descrição"
                placeholder="Descrição"
                value={item.descricao}
                onChange={(e) =>
                  atualizarItem(i, { descricao: e.target.value })
                }
              />
              <Input
                aria-label="Quantidade"
                type="number"
                min={1}
                value={item.quantidade}
                onChange={(e) =>
                  atualizarItem(i, { quantidade: Number(e.target.value) })
                }
              />
              <Selecao
                valor={item.tipo}
                aoMudar={(v) => atualizarItem(i, { tipo: v as TipoItem })}
                opcoes={[
                  { valor: "MATERIAL", rotulo: "Material" },
                  { valor: "LIVRO", rotulo: "Livro" },
                ]}
              />
              <Input
                aria-label="Link de compra"
                placeholder="Link (opcional)"
                value={item.linkCompra ?? ""}
                onChange={(e) =>
                  atualizarItem(i, { linkCompra: e.target.value })
                }
              />
              <Button
                variant="ghost"
                size="icon"
                aria-label="Remover item"
                onClick={() => setItens((l) => l.filter((_, j) => j !== i))}
              >
                <Trash2 />
              </Button>
            </div>
          ))}
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setItens((l) => [...l, novoItem()])}
            >
              <Plus /> Item
            </Button>
            <Button
              size="sm"
              disabled={
                !itens.some((i) => i.descricao.trim()) || criar.executando
              }
              onClick={() => criar.executar()}
            >
              Salvar rascunho
            </Button>
          </div>
        </CardContent>
      </Card>
    </>
  );
}
