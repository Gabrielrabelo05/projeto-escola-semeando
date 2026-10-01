import { defineConfig, devices } from "@playwright/test";

const FRONTEND = "http://localhost:3000";

/**
 * Testes ponta a ponta (navegador → Next.js → API Spring Boot).
 * A API sobe com o perfil "test" (H2 + dados sintéticos), sem precisar de Docker.
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  retries: process.env.CI ? 1 : 0,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: FRONTEND,
    trace: "retain-on-failure",
    locale: "pt-BR",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: [
    {
      command: "cd ../backend && ./mvnw -q spring-boot:test-run",
      url: "http://localhost:8080/actuator/health",
      timeout: 240_000,
      reuseExistingServer: !process.env.CI,
    },
    {
      command: "pnpm build && pnpm start",
      url: FRONTEND,
      timeout: 240_000,
      reuseExistingServer: !process.env.CI,
    },
  ],
});
