import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Imagem Docker usa o servidor standalone; localmente "next start" segue funcionando.
  output: process.env.NEXT_OUTPUT === "standalone" ? "standalone" : undefined,
  poweredByHeader: false,
};

export default nextConfig;
