"use client";

import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Confirmacao } from "@/presentation/components/shared/Confirmacao";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { PageHeader } from "@/presentation/components/shared/PageHeader";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { Tabela } from "@/presentation/components/shared/Tabela";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, moeda, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

export function FichaAlunoTela({ id }: { id: string }) {
  const { cadastro, matricula, cobranca, contrato } = useGateways();
  const ficha = useCarregar(() => cadastro.aluno(id), [id]);
  const historico = useCarregar(() => matricula.historico(id), [id]);
  const cobrancas = useCarregar(() => cobranca.doAluno(id), [id]);
  const contratos = useCarregar(
    () => contrato.pesquisar({ alunoId: id }),
    [id],
  );
  const [inativando, setInativando] = useState(false);
  const inativar = useAcao(async () => {
    await cadastro.inativarAluno(id);
    await ficha.recarregar();
  }, "Aluno inativado");

  return (
    <Conteudo estado={ficha}>
      {(a) => (
        <>
          <PageHeader
            titulo={a.nome}
            descricao={`Nascimento ${data(a.dataNascimento)} · Responsável CPF ${a.cpfResponsavel}`}
            acoes={
              <>
                <Button
                  variant="outline"
                  nativeButton={false}
                  render={<Link href="/alunos" />}
                >
                  <ArrowLeft /> Voltar
                </Button>
                {a.status !== "INATIVO" && (
                  <Button
                    variant="destructive"
                    onClick={() => setInativando(true)}
                  >
                    Inativar
                  </Button>
                )}
              </>
            }
          />
          <div className="grid gap-4 md:grid-cols-3">
            <Card size="sm">
              <CardHeader>
                <CardTitle>Situação</CardTitle>
              </CardHeader>
              <CardContent>
                <StatusBadge status={a.status} />
              </CardContent>
            </Card>
            <Card size="sm">
              <CardHeader>
                <CardTitle>Saúde</CardTitle>
              </CardHeader>
              <CardContent className="space-y-1 text-sm">
                <p>Alergias: {a.alergias || "—"}</p>
                <p>Tipo sanguíneo: {a.tipoSanguineo || "—"}</p>
                <p>Necessidades especiais: {a.necessidadesEspeciais || "—"}</p>
              </CardContent>
            </Card>
            <Card size="sm">
              <CardHeader>
                <CardTitle>Segurança</CardTitle>
              </CardHeader>
              <CardContent className="space-y-1 text-sm">
                <p>Hospital: {a.hospitalEmergencia || "—"}</p>
                {a.autorizados.map((p) => (
                  <p key={p.documento}>
                    Autorizado: {p.nome} ({p.parentesco})
                  </p>
                ))}
              </CardContent>
            </Card>
          </div>

          <Tabs defaultValue="historico">
            <TabsList>
              <TabsTrigger value="historico">Matrículas</TabsTrigger>
              <TabsTrigger value="cobrancas">Cobranças</TabsTrigger>
              <TabsTrigger value="contratos">Contratos</TabsTrigger>
            </TabsList>
            <TabsContent value="historico">
              <Conteudo estado={historico} vazio={(l) => l.length === 0}>
                {(lista) => (
                  <Tabela
                    linhas={lista}
                    chave={(m) => m.id}
                    colunas={[
                      { titulo: "Ano", celula: (m) => m.anoLetivo },
                      { titulo: "Tipo", celula: (m) => rotulo(m.tipo) },
                      {
                        titulo: "Solicitada",
                        celula: (m) => data(m.dataSolicitacao),
                      },
                      {
                        titulo: "Status",
                        celula: (m) => <StatusBadge status={m.status} />,
                      },
                    ]}
                  />
                )}
              </Conteudo>
            </TabsContent>
            <TabsContent value="cobrancas">
              <Conteudo estado={cobrancas} vazio={(l) => l.length === 0}>
                {(lista) => (
                  <Tabela
                    linhas={lista}
                    chave={(c) => c.id}
                    colunas={[
                      {
                        titulo: "Referência",
                        celula: (c) => `${rotulo(c.tipo)} ${c.mesReferencia}`,
                      },
                      {
                        titulo: "Vencimento",
                        celula: (c) => data(c.vencimento),
                      },
                      { titulo: "Valor", celula: (c) => moeda(c.valor) },
                      {
                        titulo: "Atualizado",
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
                    ]}
                  />
                )}
              </Conteudo>
            </TabsContent>
            <TabsContent value="contratos">
              <Conteudo estado={contratos} vazio={(l) => l.length === 0}>
                {(lista) => (
                  <Tabela
                    linhas={lista}
                    chave={(c) => c.id}
                    colunas={[
                      { titulo: "Documento", celula: (c) => rotulo(c.tipo) },
                      { titulo: "Versão", celula: (c) => `v${c.versao}` },
                      { titulo: "Gerado em", celula: (c) => data(c.geradoEm) },
                      {
                        titulo: "Status",
                        celula: (c) => <StatusBadge status={c.status} />,
                      },
                    ]}
                  />
                )}
              </Conteudo>
            </TabsContent>
          </Tabs>

          <Confirmacao
            aberto={inativando}
            aoFechar={() => setInativando(false)}
            titulo="Inativar aluno?"
            descricao="Registros nunca são excluídos; o histórico é mantido (RF-37)."
            destrutivo
            rotuloConfirmar="Inativar"
            aoConfirmar={() => inativar.executar()}
          />
        </>
      )}
    </Conteudo>
  );
}
