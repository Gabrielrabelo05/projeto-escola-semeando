import { expect, test } from "@playwright/test";
import { entrar } from "./apoio";

test("portal do responsável mostra filhos, boletos e lista de materiais", async ({
  page,
}) => {
  await entrar(page, "responsavel@semeando.dev");
  await expect(page).toHaveURL(/\/portal$/);
  const lucas = page.locator("[data-slot=card]", {
    hasText: "Lucas Sintético",
  });
  await expect(lucas).toBeVisible();
  await expect(
    lucas.getByRole("cell", { name: /Mensalidade/ }).first(),
  ).toBeVisible();

  await lucas.getByRole("tab", { name: "Lista de materiais" }).click();
  await expect(lucas.getByText(/Caderno brochura/)).toBeVisible();

  await lucas.getByRole("tab", { name: "Documentos" }).click();
  await expect(
    lucas.getByRole("cell", { name: "Contrato prestacao servicos" }),
  ).toBeVisible();
});

test("financeiro calcula inadimplência e gera boletos do mês", async ({
  page,
}) => {
  await entrar(page, "financeiro@semeando.dev");
  await page.getByRole("link", { name: "Financeiro" }).click();
  await page.getByRole("button", { name: /Gerar boletos de/ }).click();
  await expect(page.getByText(/boletos emitidos no Cora/)).toBeVisible();
  await page.getByRole("tab", { name: "Relatório" }).click();
  await expect(page.getByText("Previsto x recebido")).toBeVisible();
});
