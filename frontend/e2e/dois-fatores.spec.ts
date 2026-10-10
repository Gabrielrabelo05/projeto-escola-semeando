import { createHmac } from "node:crypto";
import { expect, test } from "@playwright/test";
import { entrar } from "./apoio";

const BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

/** TOTP (RFC 6238) igual ao do aplicativo autenticador, para o teste digitar o código. */
function totp(segredo: string, deslocamento = 0): string {
  let bits = "";
  for (const c of segredo.replace(/\s/g, "")) {
    bits += BASE32.indexOf(c).toString(2).padStart(5, "0");
  }
  const chave = Buffer.from(
    bits.match(/.{8}/g)?.map((b) => Number.parseInt(b, 2)) ?? [],
  );
  const passo = Buffer.alloc(8);
  passo.writeBigUInt64BE(
    BigInt(Math.floor(Date.now() / 1000 / 30) + deslocamento),
  );
  const hash = createHmac("sha1", chave).update(passo).digest();
  const inicio = hash[hash.length - 1] & 0xf;
  return ((hash.readUInt32BE(inicio) & 0x7fffffff) % 1_000_000)
    .toString()
    .padStart(6, "0");
}

test("ativa a verificação em duas etapas e passa a entrar com o código (RNF-01)", async ({
  page,
}) => {
  await entrar(page, "prestador@semeando.dev");
  await page.goto("/conta");
  await page.getByRole("button", { name: "Ativar" }).click();
  await expect(
    page.getByAltText("QR Code para o aplicativo autenticador"),
  ).toBeVisible();
  const segredo = (await page.locator("code").innerText()).trim();
  await page.getByLabel("Código do aplicativo").fill(totp(segredo));
  await page
    .getByRole("button", { name: "Ativar verificação em duas etapas" })
    .click();

  await expect(page.getByText("Guarde estes códigos")).toBeVisible();
  const recuperacao = (
    await page.getByRole("listitem").first().innerText()
  ).trim();
  const download = page.waitForEvent("download");
  await page.getByRole("button", { name: "Baixar .txt" }).click();
  await download;
  await page.getByRole("button", { name: "Já guardei, continuar" }).click();
  await expect(page.getByText("Ativa", { exact: true })).toBeVisible();

  await page.getByRole("button", { name: "Sair" }).click();
  await page.getByLabel("E-mail").fill("prestador@semeando.dev");
  await page.getByLabel("Senha").fill("Semeando@2026");
  await page.getByRole("button", { name: "Entrar" }).click();
  await page.getByLabel("Código de verificação").fill(totp(segredo, 1));
  await page.getByRole("button", { name: "Verificar" }).click();
  await expect(page).toHaveURL(/\/obrigacoes$/);

  // Desativa (com um código de recuperação) para o teste poder rodar de novo no mesmo servidor.
  await page.goto("/conta");
  await page.getByRole("button", { name: "Desativar" }).click();
  await page.getByLabel("Código de verificação").fill(recuperacao);
  await page.getByRole("button", { name: "Confirmar" }).click();
  await expect(page.getByText("Inativa", { exact: true })).toBeVisible();
});
