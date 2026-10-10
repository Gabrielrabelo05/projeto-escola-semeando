import { expect, test } from "@playwright/test";
import { entrar } from "./apoio";

test.describe("Acesso por convite e edição de cadastros (RF-35, RF-31)", () => {
  test("admin cria o acesso do professor e ele define a própria senha", async ({
    page,
  }) => {
    await entrar(page, "admin@semeando.dev");
    await page.goto("/cadastros");
    await page.getByRole("tab", { name: "Professores" }).click();
    await page
      .getByRole("row", { name: /Carlos Eduardo Nunes/ })
      .getByRole("button", { name: "Acesso" })
      .click();
    const link = await page
      .getByLabel("Link de acesso", { exact: true })
      .inputValue();
    expect(link).toContain("/definir-senha?token=");
    await page.keyboard.press("Escape");
    await page.getByRole("button", { name: "Sair" }).click();

    await page.goto(new URL(link).pathname + new URL(link).search);
    await expect(page.getByText("carlos.nunes@example.com")).toBeVisible();
    await page.getByLabel("Nova senha").fill("Professor@2026");
    await page.getByLabel("Repita a senha").fill("Professor@2026");
    await page.getByRole("button", { name: "Salvar senha" }).click();
    await expect(page).toHaveURL(/\/login$/);

    await page.getByLabel("E-mail").fill("carlos.nunes@example.com");
    await page.getByLabel("Senha").fill("Professor@2026");
    await page.getByRole("button", { name: "Entrar" }).click();
    await expect(page.getByRole("button", { name: "Sair" })).toBeVisible();
  });

  test("admin corrige o nome do aluno", async ({ page }) => {
    await entrar(page, "admin@semeando.dev");
    await page.goto("/alunos");
    await page.getByText("Isabela Carvalho Rocha").click();
    await page.getByRole("button", { name: "Editar", exact: true }).click();
    await page.locator("#aluno-nome").fill("Isabela Carvalho Rocha Lima");
    await page.getByRole("button", { name: "Salvar" }).click();
    await expect(
      page.getByRole("heading", { name: "Isabela Carvalho Rocha Lima" }),
    ).toBeVisible();
  });
});
