"use client";

import { Copy, Download } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { Contrato } from "@/domain/models/contrato";
import type { Filho } from "@/domain/models/portal";
import { ContratoVisualizacao } from "@/presentation/components/features/contratos/ContratoVisualizacao";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import {
  baixarArquivo,
  data,
  moeda,
  rotulo,
} from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

/** Portal do responsável: boletos, presença, documentos e lista de materiais. */
export function PortalTela() {
  const { portal } = useGateways();
  const filhos = useCarregar(() => portal.filhos(), []);
  return (
    <>
      <PageHeader
        titulo="Meus filhos"
        descricao="Acompanhe matrícula, frequência, boletos e documentos"
      />
      <Conteudo
        estado={filhos}
        vazio={(l) => l.length === 0}
        mensagemVazio="Nenhum aluno vinculado ao seu CPF."
      >
        {(lista) => (
          <div className="space-y-6">
            {lista.map((filho) => (
              <PainelFilho key={filho.aluno.id} filho={filho} />
            ))}
          </div>
        )}
      </Conteudo>
    </>
  );
}

function PainelFilho({ filho }: { filho: Filho }) {
  const { portal, contrato, material } = useGateways();
  const id = filho.aluno.id;
  const boletos = useCarregar(() => portal.boletos(id), [id]);
  const documentos = useCarregar(() => portal.documentos(id), [id]);
  const lista = useCarregar(
    () => material.publicada(filho.turma?.serie ?? "PRIMEIRO_ANO"),
    [filho.turma?.serie],
    !!filho.turma,
  );
  const [aberto, setAberto] = useState<Contrato | null>(null);
  const assinar = useAcao(async (c: Contrato) => {
    setAberto(await contrato.assinar(c.id));
    await documentos.recarregar();
  }, "Documento assinado eletronicamente");
  const pdf = useAcao(
    async () =>
      filho.turma && baixarArquivo(await material.baixarPdf(filho.turma.serie)),
  );

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex flex-wrap items-center gap-2">
          {filho.aluno.nome}{" "}
          {filho.statusMatricula && (
            <StatusBadge status={filho.statusMatricula} />
          )}
        </CardTitle>
        <CardDescription>
          {filho.turma?.nome ?? "Sem turma"} · Frequência{" "}
          {filho.percentualPresenca.toFixed(1)}% ({filho.faltas} faltas em{" "}
          {filho.aulas} aulas){" "}
          {filho.alertaFrequencia && (
            <Badge variant="destructive">Abaixo de 75%</Badge>
          )}
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Tabs defaultValue="boletos">
          <TabsList>
            <TabsTrigger value="boletos">Boletos</TabsTrigger>
            <TabsTrigger value="documentos">Documentos</TabsTrigger>
            <TabsTrigger value="materiais">Lista de materiais</TabsTrigger>
          </TabsList>
          <TabsContent value="boletos">
            <Conteudo
              estado={boletos}
              vazio={(l) => l.length === 0}
              mensagemVazio="Nenhum boleto."
            >
              {(lista) => (
                <Tabela
                  linhas={lista}
                  chave={(c) => c.id}
                  colunas={[
                    {
                      titulo: "Referência",
                      celula: (c) => `${rotulo(c.tipo)} ${c.mesReferencia}`,
                    },
                    { titulo: "Vencimento", celula: (c) => data(c.vencimento) },
                    {
                      titulo: "Valor",
                      celula: (c) => moeda(c.valorAtualizado),
                    },
                    {
                      titulo: "Status",
                      celula: (c) => (
                        <StatusBadge
                          status={c.vencida ? "VENCIDA" : c.status}
                        />
                      ),
                    },
                    {
                      titulo: "",
                      celula: (c) =>
                        c.status === "EMITIDA" &&
                        c.linhaDigitavel && (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => {
                              void navigator.clipboard.writeText(
                                c.linhaDigitavel ?? "",
                              );
                              toast.success("Linha digitável copiada");
                            }}
                          >
                            <Copy /> Copiar código
                          </Button>
                        ),
                    },
                  ]}
                />
              )}
            </Conteudo>
          </TabsContent>
          <TabsContent value="documentos">
            <Conteudo
              estado={documentos}
              vazio={(l) => l.length === 0}
              mensagemVazio="Nenhum documento."
            >
              {(lista) => (
                <Tabela
                  linhas={lista}
                  chave={(c) => c.id}
                  aoClicar={setAberto}
                  colunas={[
                    { titulo: "Documento", celula: (c) => rotulo(c.tipo) },
                    { titulo: "Versão", celula: (c) => `v${c.versao}` },
                    {
                      titulo: "Status",
                      celula: (c) => <StatusBadge status={c.status} />,
                    },
                  ]}
                />
              )}
            </Conteudo>
          </TabsContent>
          <TabsContent value="materiais">
            <Conteudo estado={lista}>
              {(l) => (
                <div className="space-y-2">
                  <ul className="space-y-1 text-sm">
                    {l.itens.map((item) => (
                      <li key={item.descricao}>
                        {item.quantidade}× {item.descricao}{" "}
                        {item.linkCompra && (
                          <a
                            href={item.linkCompra}
                            target="_blank"
                            rel="noreferrer"
                            className="text-xs underline"
                          >
                            onde comprar
                          </a>
                        )}
                      </li>
                    ))}
                  </ul>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => pdf.executar()}
                  >
                    <Download /> Baixar PDF
                  </Button>
                </div>
              )}
            </Conteudo>
          </TabsContent>
        </Tabs>
      </CardContent>
      <ContratoVisualizacao
        contrato={aberto}
        aoFechar={() => setAberto(null)}
        aoAssinar={(c) => assinar.executar(c)}
      />
    </Card>
  );
}
