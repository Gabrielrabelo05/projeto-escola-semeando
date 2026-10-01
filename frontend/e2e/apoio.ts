import { expect, type Page } from "@playwright/test";

export const SENHA = "Semeando@2026";

export async function entrar(page: Page, email: string) {
  await page.goto("/login");
  await page.getByLabel("E-mail").fill(email);
  await page.getByLabel("Senha").fill(SENHA);
  await page.getByRole("button", { name: "Entrar" }).click();
  await expect(page.getByRole("button", { name: "Sair" })).toBeVisible();
}

/** Gera um CPF sintético válido (RN-08: nunca usar dados reais). */
export function cpfSintetico(): string {
  const base = Array.from({ length: 9 }, () => Math.floor(Math.random() * 10));
  const digito = (numeros: number[]) => {
    const soma = numeros.reduce(
      (total, n, i) => total + n * (numeros.length + 1 - i),
      0,
    );
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };
  const primeiro = digito(base);
  return [...base, primeiro, digito([...base, primeiro])].join("");
}
