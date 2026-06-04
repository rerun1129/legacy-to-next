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
    <html lang={locale} className={`h-full ${inter.variable}`} suppressHydrationWarning>
      {/* blocking inline script: hydration 전에 실행되어 FOUC 없이 다크모드를 적용한다.
          서버 렌더 시 <html>에 data-theme가 없으므로 suppressHydrationWarning 으로 경고를 억제한다. */}
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{if(localStorage.getItem('theme')==='dark'){document.documentElement.setAttribute('data-theme','dark');}}catch(e){}})();`,
          }}
        />
      </head>
      <body className="h-full">
        <NextIntlClientProvider>
          <QueryProvider>{children}</QueryProvider>
          <ConfirmModalRoot />
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
