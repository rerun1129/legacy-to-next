import type { NextConfig } from "next";
import createNextIntlPlugin from "next-intl/plugin";

// Default config path: ./src/i18n/request.ts (next-intl convention)
const withNextIntl = createNextIntlPlugin();

const nextConfig: NextConfig = {
  output: "standalone",
};

export default withNextIntl(nextConfig);
