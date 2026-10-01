import { expect, test } from "@playwright/test";
import { entrar } from "./apoio";

test.describe("Autenticação e perfis de acesso (RF-35)", () => {
  test("credenciais inválidas exibem erro", async ({ page }) => {
    await page.goto("/login");
    await page.getByLabel("E-mail").fill("secretaria@semeando.dev");
    await page.getByLabel("Senha").fill("senha-errada");
    await page.getByRole("button", { name: "Entrar" }).click();
    await expect(page.getByText("E-mail ou senha inválidos")).toBeVisible();
  });

  test("rota restrita sem login redireciona para o login", async ({ page }) => {
    await page.goto("/matriculas");
    await expect(page).toHaveURL(/\/login$/);
  });

  test("professor não enxerga o módulo financeiro", async ({ page }) => {
    await entrar(page, "professora@semeando.dev");
    const menu = page.getByRole("navigation", { name: "Menu principal" });
    await expect(menu.getByRole("link", { name: "Pedagógico" })).toBeVisible();
    await expect(menu.getByRole("link", { name: "Financeiro" })).toHaveCount(0);

    await page.goto("/financeiro");
    await expect(page).toHaveURL(/\/painel$/);
  });
});
