import type { NextConfig } from "next";

/**
 * O navegador fala só com este domínio: "/api/*" é repassado ao backend pelo próprio Next. Assim o cookie de
 * sessão (HttpOnly, SameSite=Strict) é de primeira parte e o token nunca fica acessível ao JavaScript.
 */
const BACKEND =
  process.env.BACKEND_URL ??
  process.env.NEXT_PUBLIC_API_URL ??
  "http://localhost:8080/api";

const desenvolvimento = process.env.NODE_ENV !== "production";

/** Bloqueia scripts/conexões de outras origens, iframes e plugins (mitiga XSS e clickjacking). */
const CSP = [
  "default-src 'self'",
  `script-src 'self' 'unsafe-inline'${desenvolvimento ? " 'unsafe-eval'" : ""}`,
  "style-src 'self' 'unsafe-inline'",
  "img-src 'self' data: blob:",
  "font-src 'self' data:",
  "connect-src 'self'",
  "frame-ancestors 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "object-src 'none'",
].join("; ");

const nextConfig: NextConfig = {
  // Imagem Docker usa o servidor standalone; localmente "next start" segue funcionando.
  output: process.env.NEXT_OUTPUT === "standalone" ? "standalone" : undefined,
  poweredByHeader: false,
  async rewrites() {
    return [{ source: "/api/:caminho*", destination: `${BACKEND}/:caminho*` }];
  },
  async headers() {
    return [
      {
        source: "/:caminho*",
        headers: [
          { key: "Content-Security-Policy", value: CSP },
          {
            key: "Strict-Transport-Security",
            value: "max-age=63072000; includeSubDomains",
          },
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "X-Frame-Options", value: "DENY" },
          // O link de definição de senha leva um token na URL: nunca enviar a URL para outros sites.
          { key: "Referrer-Policy", value: "no-referrer" },
          {
            key: "Permissions-Policy",
            value: "camera=(), microphone=(), geolocation=(), payment=()",
          },
        ],
      },
    ];
  },
};

export default nextConfig;
