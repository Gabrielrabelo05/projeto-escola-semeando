"use client";

import { Bell, Check, Upload, X } from "lucide-react";
import { useRef, useState } from "react";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import type { UUID } from "@/domain/models/comum";
import type { TipoDocumento } from "@/domain/models/matricula";
import { Confirmacao } from "@/presentation/components/shared/Confirmacao";
import { Conteudo } from "@/presentation/components/shared/Estados";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { StatusBadge } from "@/presentation/components/shared/StatusBadge";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useCarregar } from "@/presentation/hooks/useCarregar";
import { data, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";
import { FardaForm } from "./FardaForm";

interface Props {
  matriculaId: UUID | null;
  aoFechar: () => void;
  aoAlterar: () => void;
}

type Dialogo = "aprovar" | "rejeitar" | "cancelar" | null;

export function MatriculaDetalhe({ matriculaId, aoFechar, aoAlterar }: Props) {
  const { matricula, cadastro } = useGateways();
  const estado = useCarregar(
    () => matricula.detalhar(matriculaId as UUID),
    [matriculaId],
    matriculaId !== null,
  );
  const turmas = useCarregar(() => cadastro.turmas(), []);
  const [dialogo, setDialogo] = useState<Dialogo>(null);
  const [destino, setDestino] = useState<string>();
  const arquivo = useRef<HTMLInputElement>(null);
  const [tipoUpload, setTipoUpload] = useState<TipoDocumento | null>(null);

  const atualizar = async () => {
    await estado.recarregar();
    aoAlterar();
  };
  const aprovar = useAcao(async () => {
    const contrato = await matricula.aprovar(matriculaId as UUID);
    toast.success(
      `Matrícula aprovada. ${rotulo(contrato.tipo)} gerado e primeira parcela emitida.`,
    );
    await atualizar();
  });
  const transferir = useAcao(async () => {
    await matricula.transferir(matriculaId as UUID, destino as UUID);
    await atualizar();
  }, "Aluno transferido de turma");
  const notificar = useAcao(
    () => matricula.notificarPendencias(matriculaId as UUID),
    "Responsável notificado",
  );
  const enviarDocumento = useAcao(async (tipo: TipoDocumento, file: File) => {
    await matricula.enviarDocumento(matriculaId as UUID, tipo, file);
    await atualizar();
  }, "Documento anexado");

  function escolherArquivo(tipo: TipoDocumento) {
    setTipoUpload(tipo);
    arquivo.current?.click();
  }

  return (
    <Sheet
      open={matriculaId !== null}
      onOpenChange={(aberto) => !aberto && aoFechar()}
    >
      <SheetContent className="w-full overflow-y-auto sm:max-w-xl">
        <Conteudo estado={estado}>
          {(m) => {
            const opcoesTurma = (turmas.dados ?? [])
              .filter(
                (t) =>
                  t.anoLetivo === m.anoLetivo &&
                  t.nivel === m.nivel &&
                  t.id !== m.turmaId,
              )
              .map((t) => ({ valor: t.id, rotulo: t.nome }));
            return (
              <div className="space-y-5 p-4">
                <SheetHeader className="p-0">
                  <SheetTitle>{m.aluno?.nome}</SheetTitle>
                  <SheetDescription>
                    {m.turma?.nome} · {rotulo(m.nivel)} · {m.anoLetivo}
                  </SheetDescription>
                  <div className="flex gap-2">
                    <StatusBadge status={m.status} />
                    <Badge variant="outline">{rotulo(m.tipo)}</Badge>
                  </div>
                </SheetHeader>

                {m.justificativaRejeicao && (
                  <p className="rounded-lg bg-destructive/10 p-3 text-sm text-destructive">
                    Motivo da rejeição: {m.justificativaRejeicao}
                  </p>
                )}

                {m.status === "AGUARDANDO_ANALISE" && (
                  <div className="flex gap-2">
                    <Button onClick={() => setDialogo("aprovar")}>
                      <Check /> Aprovar
                    </Button>
                    <Button
                      variant="destructive"
                      onClick={() => setDialogo("rejeitar")}
                    >
                      <X /> Rejeitar
                    </Button>
                  </div>
                )}

                <Separator />
                <section className="space-y-2">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-semibold">
                      Documentos obrigatórios (RN-05)
                    </h3>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => notificar.executar()}
                      disabled={notificar.executando}
                    >
                      <Bell /> Cobrar pendências
                    </Button>
                  </div>
                  <ul className="divide-y rounded-lg border">
                    {m.documentos.map((d) => (
                      <li
                        key={d.tipo}
                        className="flex items-center justify-between gap-2 px-3 py-2 text-sm"
                      >
                        <div>
                          <p>{d.descricao}</p>
                          <p className="text-xs text-muted-foreground">
                            {d.entregue
                              ? `Entregue em ${data(d.entregueEm)}`
                              : "Não entregue"}
                            {d.entregue && d.pendente && " · vencido"}
                          </p>
                        </div>
                        <div className="flex items-center gap-2">
                          {d.pendente ? (
                            <Badge variant="outline">Pendente</Badge>
                          ) : (
                            <Badge>OK</Badge>
                          )}
                          <Button
                            size="icon-sm"
                            variant="ghost"
                            aria-label={`Enviar ${d.descricao}`}
                            onClick={() => escolherArquivo(d.tipo)}
                          >
                            <Upload />
                          </Button>
                        </div>
                      </li>
                    ))}
                  </ul>
                  <input
                    ref={arquivo}
                    type="file"
                    hidden
                    accept=".pdf,.png,.jpg,.jpeg"
                    onChange={(e) => {
                      const file = e.target.files?.[0];
                      if (file && tipoUpload)
                        void enviarDocumento.executar(tipoUpload, file);
                      e.target.value = "";
                    }}
                  />
                </section>

                <Separator />
                <section className="space-y-2">
                  <h3 className="text-sm font-semibold">Farda (RF-04)</h3>
                  <FardaForm
                    matriculaId={m.id}
                    farda={m.farda}
                    aoSalvar={atualizar}
                  />
                </section>

                {m.status === "ATIVA" && (
                  <>
                    <Separator />
                    <section className="space-y-2">
                      <h3 className="text-sm font-semibold">
                        Transferir de turma (RN-04)
                      </h3>
                      <div className="flex gap-2">
                        <Selecao
                          valor={destino}
                          opcoes={opcoesTurma}
                          aoMudar={setDestino}
                          placeholder="Turma de destino"
                        />
                        <Button
                          disabled={!destino || transferir.executando}
                          onClick={() => transferir.executar()}
                        >
                          Transferir
                        </Button>
                      </div>
                    </section>
                  </>
                )}

                {!["CANCELADA", "REJEITADA", "TRANSFERIDA"].includes(
                  m.status,
                ) && (
                  <Button
                    variant="ghost"
                    className="text-destructive"
                    onClick={() => setDialogo("cancelar")}
                  >
                    Cancelar matrícula
                  </Button>
                )}
              </div>
            );
          }}
        </Conteudo>
      </SheetContent>

      <Confirmacao
        aberto={dialogo === "aprovar"}
        aoFechar={() => setDialogo(null)}
        titulo="Aprovar matrícula?"
        descricao="O sistema vai gerar o contrato/ficha de sócio conforme o nível de ensino (RN-01) e a primeira parcela, cujo pagamento confirma a matrícula: boleto no Cora para o Fundamental e taxa de associado paga na secretaria para o Infantil."
        rotuloConfirmar="Aprovar"
        aoConfirmar={() => aprovar.executar()}
      />
      <Confirmacao
        aberto={dialogo === "rejeitar"}
        aoFechar={() => setDialogo(null)}
        titulo="Rejeitar matrícula"
        descricao="A justificativa fica registrada e o responsável é notificado."
        pedirTexto="Justificativa"
        rotuloConfirmar="Rejeitar"
        destrutivo
        aoConfirmar={async (justificativa) => {
          await matricula.rejeitar(matriculaId as UUID, justificativa);
          toast.success("Matrícula rejeitada");
          await atualizar();
        }}
      />
      <Confirmacao
        aberto={dialogo === "cancelar"}
        aoFechar={() => setDialogo(null)}
        titulo="Cancelar matrícula?"
        descricao="O registro não é excluído: matrícula e aluno ficam com status cancelado (RF-37)."
        rotuloConfirmar="Cancelar matrícula"
        destrutivo
        aoConfirmar={async () => {
          await matricula.cancelar(matriculaId as UUID);
          toast.success("Matrícula cancelada");
          await atualizar();
        }}
      />
    </Sheet>
  );
}
