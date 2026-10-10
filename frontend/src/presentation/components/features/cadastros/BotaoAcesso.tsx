"use client";

import { KeyRound } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import type { ConviteAcesso } from "@/domain/models/auth";
import type { Perfil } from "@/domain/models/comum";
import { LinkAcessoDialog } from "@/presentation/components/shared/LinkAcessoDialog";
import { useAcao } from "@/presentation/hooks/useAcao";
import { somenteDigitos } from "@/presentation/lib/formatadores";
import { useAuth } from "@/presentation/providers/AuthProvider";
import { useGateways } from "@/presentation/providers/GatewayProvider";

interface Props {
  nome: string;
  email: string | null;
  perfil: Perfil;
  cpf?: string | null;
}

/**
 * Cria o login de quem foi cadastrado (somente o administrador). Se o e-mail já tem acesso, gera um novo link,
 * que também serve para redefinir a senha.
 */
export function BotaoAcesso({ nome, email, perfil, cpf }: Props) {
  const { auth } = useGateways();
  const { possuiPerfil } = useAuth();
  const [convite, setConvite] = useState<ConviteAcesso | null>(null);
  const gerar = useAcao(async () => {
    setConvite(
      await auth.criarUsuario({
        nome,
        email: email ?? "",
        perfil,
        cpf: cpf ? somenteDigitos(cpf) : undefined,
      }),
    );
  });
  if (!possuiPerfil("ADMIN")) return null;
  // Os eventos do diálogo (portal) sobem pela árvore do React: não podem abrir a edição da linha.
  return (
    // biome-ignore lint/a11y/noStaticElementInteractions: só bloqueia a propagação do clique
    // biome-ignore lint/a11y/useKeyWithClickEvents: idem
    <span onClick={(ev) => ev.stopPropagation()}>
      <Button
        size="sm"
        variant="outline"
        disabled={!email || gerar.executando}
        title={email ? undefined : "Cadastre o e-mail para criar o acesso"}
        onClick={() => void gerar.executar()}
      >
        <KeyRound /> Acesso
      </Button>
      <LinkAcessoDialog convite={convite} aoFechar={() => setConvite(null)} />
    </span>
  );
}
