import { Suspense } from "react";
import { DefinirSenhaTela } from "@/presentation/components/features/auth/DefinirSenhaTela";

export default function DefinirSenhaPage() {
  return (
    <Suspense>
      <DefinirSenhaTela />
    </Suspense>
  );
}
