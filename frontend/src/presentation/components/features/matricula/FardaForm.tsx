"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import type { UUID } from "@/domain/models/comum";
import type { Farda } from "@/domain/models/matricula";
import { CampoTexto } from "@/presentation/components/shared/Campo";
import { useAcao } from "@/presentation/hooks/useAcao";
import { useGateways } from "@/presentation/providers/GatewayProvider";

const schema = z.object({
  blusa: z.string().trim().min(1, "Informe"),
  calca: z.string().trim().min(1, "Informe"),
  tenis: z.string().trim().min(1, "Informe"),
});

export function FardaForm({
  matriculaId,
  farda,
  aoSalvar,
}: {
  matriculaId: UUID;
  farda: Farda | null;
  aoSalvar: () => void;
}) {
  const { matricula } = useGateways();
  const { register, handleSubmit, formState } = useForm<Farda>({
    resolver: zodResolver(schema),
    values: farda ?? { blusa: "", calca: "", tenis: "" },
  });
  const salvar = useAcao(async (dados: Farda) => {
    await matricula.registrarFarda(matriculaId, dados);
    aoSalvar();
  }, "Tamanhos de farda salvos");

  return (
    <form
      onSubmit={handleSubmit((d) => salvar.executar(d))}
      className="grid grid-cols-[1fr_1fr_1fr_auto] items-end gap-2"
    >
      <CampoTexto
        id="blusa"
        rotulo="Blusa"
        erro={formState.errors.blusa?.message}
        {...register("blusa")}
      />
      <CampoTexto
        id="calca"
        rotulo="Calça"
        erro={formState.errors.calca?.message}
        {...register("calca")}
      />
      <CampoTexto
        id="tenis"
        rotulo="Tênis"
        erro={formState.errors.tenis?.message}
        {...register("tenis")}
      />
      <Button type="submit" variant="outline" disabled={salvar.executando}>
        Salvar
      </Button>
    </form>
  );
}
