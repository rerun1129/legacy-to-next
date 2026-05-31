"use client";

import { useTranslations } from "next-intl";
import { NOTICE_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

const catClass: Record<string, string> = { urgent: "urgent", update: "update", event: "event" };

export function Notices() {
  const t = useTranslations("fms.dashboard");

  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          {t("panels.notices")}
        </div>
      </div>
      <div className="dash-panel__body--flush notice-list">
        <table>
          <thead>
            <tr>
              <th>{t("notices.cols.category")}</th>
              <th className="title">{t("notices.cols.title")}</th>
              <th>{t("notices.cols.date")}</th>
            </tr>
          </thead>
          <tbody>
            {NOTICE_DATA.map((n) => (
              <tr key={n.title}>
                <td>
                  {n.pinned && <span className="pin" style={{ marginRight: 4 }}>📌</span>}
                  <span className={`cat ${catClass[n.cat]}`}>{n.cat}</span>
                </td>
                <td className="title">{n.title}</td>
                <td className="date">{n.date}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
