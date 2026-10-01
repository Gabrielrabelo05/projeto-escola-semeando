/** Conventional Commits obrigatório: feat, fix, docs, refactor, test, chore… */
export default {
  extends: ["@commitlint/config-conventional"],
  rules: {
    "scope-enum": [
      1,
      "always",
      ["backend", "frontend", "matricula", "contrato", "cobranca", "cadastro", "pedagogico", "obrigacao", "infra", "docs"],
    ],
  },
};
