import type { Metadata } from "next";
import { Inter } from "next/font/google";
import { NextIntlClientProvider } from "next-intl";
import { getLocale } from "next-intl/server";
import { QueryProvider } from "@/components/providers/query-provider";
import { ConfirmModalRoot } from "@/components/confirm";
import "./globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

export const metadata: Metadata = {
  title: "FMS — Forwarding Management System",
  description: "화물 운송 관리 시스템",
};

export default async function RootLayout({ children }: { children: React.ReactNode }) {
  const locale = await getLocale();
  return (
    <html lang={locale} className={`h-full ${inter.variable}`}>
      <body className="h-full">
        <NextIntlClientProvider>
          <QueryProvider>{children}</QueryProvider>
          <ConfirmModalRoot />
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
