"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { FieldGroup } from "@/components/ui/field";
import type { Cobranca, FormaPagamento } from "@/domain/models/cobranca";
import { Campo, CampoTexto } from "@/presentation/components/shared/Campo";
import { Selecao } from "@/presentation/components/shared/Selecao";
import { useAcao } from "@/presentation/hooks/useAcao";
import { hojeISO, moeda, rotulo } from "@/presentation/lib/formatadores";
import { useGateways } from "@/presentation/providers/GatewayProvider";

const FORMAS: FormaPagamento[] = [
  "PIX",
  "DINHEIRO",
  "CARTAO",
  "TRANSFERENCIA",
  "BOLETO",
];

const schema = z.object({
  pagoEm: z.string().min(1, "Informe a data"),
  valorPago: z.coerce.number<string>().positive("Valor inválido"),
  forma: z.enum(FORMAS),
});

type Form = z.input<typeof schema>;

/** RF-27: registro de pagamento feito fora do boleto (dinheiro, PIX direto…). */
export function PagamentoManualDialog({
  cobranca,
  aoFechar,
  aoSalvar,
}: {
  cobranca: Cobranca | null;
  aoFechar: () => void;
  aoSalvar: () => void;
}) {
  const { cobranca: gateway } = useGateways();
  const { register, control, handleSubmit, formState } = useForm<
    Form,
    unknown,
    z.output<typeof schema>
  >({
    resolver: zodResolver(schema),
    values: {
      pagoEm: hojeISO(),
      valorPago: String(cobranca?.valorAtualizado ?? ""),
      forma: "PIX",
    },
  });
  const salvar = useAcao(async (dados: z.output<typeof schema>) => {
    await gateway.registrarPagamento(cobranca?.id as string, dados);
    aoSalvar();
    aoFechar();
  }, "Pagamento registrado");

  return (
    <Dialog open={cobranca !== null} onOpenChange={(a) => !a && aoFechar()}>
      <DialogContent>
        <form
          onSubmit={handleSubmit((d) => salvar.executar(d))}
          className="grid gap-4"
        >
          <DialogHeader>
            <DialogTitle>Registrar pagamento manual</DialogTitle>
            <DialogDescription>
              {cobranca?.aluno?.nome} · {rotulo(cobranca?.tipo)}{" "}
              {cobranca?.mesReferencia} · original {moeda(cobranca?.valor)}
            </DialogDescription>
          </DialogHeader>
          <FieldGroup>
            <CampoTexto
              id="pago-em"
              rotulo="Data do pagamento"
              type="date"
              erro={formState.errors.pagoEm?.message}
              {...register("pagoEm")}
            />
            <CampoTexto
              id="valor-pago"
              rotulo="Valor pago"
              inputMode="decimal"
              erro={formState.errors.valorPago?.message}
              {...register("valorPago")}
            />
            <Controller
              control={control}
              name="forma"
              render={({ field }) => (
                <Campo id="forma" rotulo="Forma de pagamento">
                  <Selecao
                    id="forma"
                    valor={field.value}
                    aoMudar={field.onChange}
                    opcoes={FORMAS.map((f) => ({
                      valor: f,
                      rotulo: rotulo(f),
                    }))}
                  />
                </Campo>
              )}
            />
          </FieldGroup>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={aoFechar}>
              Cancelar
            </Button>
            <Button type="submit" disabled={salvar.executando}>
              Registrar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
