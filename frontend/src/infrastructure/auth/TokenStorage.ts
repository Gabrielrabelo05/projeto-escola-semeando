import type { Sessao } from "@/domain/models/auth";

export interface TokenStorage {
  ler(): Sessao | null;
  salvar(sessao: Sessao): void;
  limpar(): void;
}

const CHAVE = "semeando.sessao";

export class LocalStorageTokenStorage implements TokenStorage {
  ler(): Sessao | null {
    if (typeof window === "undefined") return null;
    try {
      const bruto = window.localStorage.getItem(CHAVE);
      return bruto ? (JSON.parse(bruto) as Sessao) : null;
    } catch {
      return null;
    }
  }

  salvar(sessao: Sessao): void {
    window.localStorage.setItem(CHAVE, JSON.stringify(sessao));
  }

  limpar(): void {
    window.localStorage.removeItem(CHAVE);
  }
}

export class MemoryTokenStorage implements TokenStorage {
  private sessao: Sessao | null = null;

  ler(): Sessao | null {
    return this.sessao;
  }

  salvar(sessao: Sessao): void {
    this.sessao = sessao;
  }

  limpar(): void {
    this.sessao = null;
  }
}
