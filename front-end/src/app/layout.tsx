import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "FMS — Forwarding Management System",
  description: "화물 운송 관리 시스템",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko" className="h-full">
      <body className="h-full">{children}</body>
    </html>
  );
}
