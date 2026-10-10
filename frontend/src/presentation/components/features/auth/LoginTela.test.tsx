import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import type { Gateways } from "@/domain/gateways";
import { ErroApi } from "@/domain/models/erro";
import { AuthProvider } from "@/presentation/providers/AuthProvider";
import { GatewayProvider } from "@/presentation/providers/GatewayProvider";
import { LoginTela } from "./LoginTela";

const replace = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace, push: vi.fn() }),
}));

function renderizar(
  entrar: Gateways["auth"]["entrar"],
  confirmarCodigo: Gateways["auth"]["confirmarCodigo"] = vi.fn(),
) {
  const auth = {
    entrar,
    confirmarCodigo,
    prepararDoisFatoresNoLogin: vi.fn(),
    ativarDoisFatoresNoLogin: vi.fn(),
    situacaoDoisFatores: vi.fn(),
    prepararDoisFatores: vi.fn(),
    ativarDoisFatores: vi.fn(),
    desativarDoisFatores: vi.fn(),
    sessaoAtual: vi.fn().mockRejectedValue(new ErroApi(401, "HTTP_401", "")),
    sair: vi.fn().mockResolvedValue(undefined),
    listarUsuarios: vi.fn(),
    criarUsuario: vi.fn(),
    novoLinkAcesso: vi.fn(),
    inativarUsuario: vi.fn(),
    consultarConvite: vi.fn(),
    definirSenha: vi.fn(),
  };
  const gateways = { auth } as unknown as Gateways;
  return render(
    <GatewayProvider gateways={gateways}>
      <AuthProvider gateway={auth}>
        <LoginTela />
      </AuthProvider>
    </GatewayProvider>,
  );
}

describe("LoginTela", () => {
  beforeEach(() => replace.mockReset());

  it("valida os campos antes de chamar o gateway", async () => {
    const entrar = vi.fn();
    renderizar(entrar);
    await userEvent.click(screen.getByRole("button", { name: "Entrar" }));
    expect(
      await screen.findByText("Informe um e-mail válido"),
    ).toBeInTheDocument();
    expect(entrar).not.toHaveBeenCalled();
  });

  it("autentica pelo gateway e redireciona conforme o perfil", async () => {
    const entrar = vi.fn().mockResolvedValue({
      etapa: "AUTENTICADO",
      usuario: {
        id: "1",
        nome: "Maria",
        email: "r@x.dev",
        perfil: "RESPONSAVEL",
        ativo: true,
      },
    });
    renderizar(entrar);
    await userEvent.type(screen.getByLabelText("E-mail"), "r@x.dev");
    await userEvent.type(screen.getByLabelText("Senha"), "segredo123");
    await userEvent.click(screen.getByRole("button", { name: "Entrar" }));

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/portal"));
    expect(entrar).toHaveBeenCalledWith({
      email: "r@x.dev",
      senha: "segredo123",
    });
  });

  it("exibe a mensagem de erro devolvida pela API", async () => {
    renderizar(
      vi
        .fn()
        .mockRejectedValue(
          new ErroApi(
            401,
            "CREDENCIAIS_INVALIDAS",
            "E-mail ou senha inválidos",
          ),
        ),
    );
    await userEvent.type(screen.getByLabelText("E-mail"), "x@x.dev");
    await userEvent.type(screen.getByLabelText("Senha"), "errada");
    await userEvent.click(screen.getByRole("button", { name: "Entrar" }));
    expect(
      await screen.findByText("E-mail ou senha inválidos"),
    ).toBeInTheDocument();
  });

  it("pede o código do aplicativo quando a conta tem verificação em duas etapas", async () => {
    const entrar = vi.fn().mockResolvedValue({ etapa: "CODIGO" });
    const confirmarCodigo = vi.fn().mockResolvedValue({
      usuario: {
        id: "1",
        nome: "Renata",
        email: "admin@x.dev",
        perfil: "ADMIN",
        ativo: true,
      },
    });
    renderizar(entrar, confirmarCodigo);
    await userEvent.type(screen.getByLabelText("E-mail"), "admin@x.dev");
    await userEvent.type(screen.getByLabelText("Senha"), "segredo123");
    await userEvent.click(screen.getByRole("button", { name: "Entrar" }));

    expect(replace).not.toHaveBeenCalled();
    await userEvent.type(
      await screen.findByLabelText("Código de verificação"),
      "123456",
    );
    await userEvent.click(screen.getByRole("button", { name: "Verificar" }));

    await waitFor(() => expect(replace).toHaveBeenCalledWith("/painel"));
    expect(confirmarCodigo).toHaveBeenCalledWith("123456");
  });
});
