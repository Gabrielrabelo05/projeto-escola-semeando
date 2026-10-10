"use client";

import { CampoTexto } from "@/presentation/components/shared/Campo";

interface Props {
  valor: string;
  aoMudar(valor: string): void;
  /** Só o código de 6 dígitos (na ativação não existem códigos de recuperação ainda). */
  somenteAplicativo?: boolean;
}

/** Código do aplicativo autenticador ou, no login, um código de recuperação (XXXXX-XXXXX). */
export function CampoCodigo({ valor, aoMudar, somenteAplicativo }: Props) {
  return (
    <CampoTexto
      id="codigo"
      rotulo={
        somenteAplicativo ? "Código do aplicativo" : "Código de verificação"
      }
      descricao={
        somenteAplicativo
          ? undefined
          : "6 dígitos do aplicativo autenticador, ou um código de recuperação."
      }
      autoComplete="one-time-code"
      inputMode={somenteAplicativo ? "numeric" : "text"}
      maxLength={somenteAplicativo ? 6 : 11}
      autoFocus
      value={valor}
      onChange={(e) =>
        aoMudar(
          somenteAplicativo
            ? e.target.value.replace(/\D/g, "")
            : e.target.value.toUpperCase(),
        )
      }
    />
  );
}
