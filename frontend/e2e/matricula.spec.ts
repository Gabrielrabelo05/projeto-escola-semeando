import { expect, test } from "@playwright/test";
import { cpfSintetico, entrar } from "./apoio";

test.describe
  .serial("Fluxo de matrícula (seção 9 do levantamento)", () => {
    const aluno = `Aluno E2E ${Date.now()}`;

    test("pré-matrícula pública com validação da RN-06", async ({ page }) => {
      await page.goto("/pre-matricula");
      await page.getByLabel("Nome completo do aluno").fill(aluno);
      await page.getByLabel("Data de nascimento").fill("2019-03-15");
      await page.getByRole("button", { name: "Continuar" }).click();

      await page.getByRole("button", { name: "Continuar" }).click();
      await expect(
        page.getByText("Hospital de emergência é obrigatório"),
      ).toBeVisible();
      await page
        .getByLabel("Hospital de emergência")
        .fill("Hospital Sintético");
      await page.getByLabel("Nome", { exact: true }).fill("Tia Sintética");
      await page.getByLabel("Documento").fill("RG 0001");
      await page.getByLabel("Parentesco").fill("Tia");
      await page.getByRole("button", { name: "Continuar" }).click();

      await page.getByLabel("Nome do responsável").fill("Responsável E2E");
      await page.getByLabel("CPF").fill(cpfSintetico());
      await page.getByLabel("Parentesco").fill("Mãe");
      await page.getByLabel("E-mail").fill("responsavel.e2e@exemplo.dev");
      await page.getByLabel("Telefone (WhatsApp)").fill("98912345678");
      await page.getByRole("button", { name: "Continuar" }).click();

      await page.getByRole("combobox").click();
      await page.getByRole("option", { name: /2º ano/ }).click();
      await page.getByRole("button", { name: "Enviar pré-matrícula" }).click();
      await expect(page.getByText("Pré-matrícula enviada!")).toBeVisible();
    });

    test("secretaria aprova e o contrato do Ensino Fundamental é gerado (RF-02, RN-01)", async ({
      page,
    }) => {
      await entrar(page, "secretaria@semeando.dev");
      await page.getByRole("link", { name: "Matrículas" }).click();
      await page.getByRole("cell", { name: aluno }).click();
      await expect(
        page.getByText("Documentos obrigatórios (RN-05)"),
      ).toBeVisible();
      await page.getByRole("button", { name: "Aprovar" }).first().click();
      await page
        .getByRole("dialog", { name: "Aprovar matrícula?" })
        .getByRole("button", { name: "Aprovar" })
        .click();
      await expect(
        page.getByText(
          /Matrícula aprovada\. Contrato prestacao servicos gerado/,
        ),
      ).toBeVisible();
    });

    test("contrato com valor divergente da tabela oficial é bloqueado (RN-02)", async ({
      page,
    }) => {
      await entrar(page, "secretaria@semeando.dev");
      await page.getByRole("link", { name: "Contratos" }).click();
      const cartao = page.locator("[data-slot=card]", {
        hasText: "Gerar contrato com valores informados",
      });
      await cartao.getByRole("combobox").click();
      await page.getByRole("option", { name: new RegExp(aluno) }).click();
      await cartao.getByLabel("Valor da parcela").fill("350.00");
      await cartao.getByRole("button", { name: "Validar e gerar" }).click();
      await expect(
        page.getByText(/RN-02: Valores divergentes da tabela oficial/),
      ).toBeVisible();
    });
  });
