"use client";

import { useState } from "react";
import { holidayData } from "@/adapter/out/mock/mock-data";

const DOW = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

function buildCalendar(year: number, month: number) {
  const firstDay = new Date(year, month, 1).getDay();
  const daysInMonth = new Date(year, month + 1, 0).getDate();
  const daysInPrev = new Date(year, month, 0).getDate();
  const cells: { day: number; current: boolean }[] = [];
  for (let i = firstDay - 1; i >= 0; i--) {
    cells.push({ day: daysInPrev - i, current: false });
  }
  for (let d = 1; d <= daysInMonth; d++) {
    cells.push({ day: d, current: true });
  }
  const remaining = 42 - cells.length;
  for (let d = 1; d <= remaining; d++) {
    cells.push({ day: d, current: false });
  }
  return cells;
}

export function HolidayCalendar() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth());
  const today = now.getDate();

  const cells = buildCalendar(year, month);
  const monthLabel = new Date(year, month).toLocaleString("en", { month: "long", year: "numeric" });

  const prev = () => { if (month === 0) { setYear(y => y - 1); setMonth(11); } else setMonth(m => m - 1); };
  const next = () => { if (month === 11) { setYear(y => y + 1); setMonth(0); } else setMonth(m => m + 1); };

  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Public Holidays
        </div>
      </div>
      <div className="mini-cal">
        <div className="mini-cal__head">
          <span className="mini-cal__title">{monthLabel}</span>
          <div className="mini-cal__nav">
            <button onClick={prev}>‹</button>
            <button onClick={next}>›</button>
          </div>
        </div>
        <div className="mini-cal__grid">
          {DOW.map((d, i) => (
            <div key={d} className={`mini-cal__dow ${i === 0 ? "sun" : i === 6 ? "sat" : ""}`}>{d[0]}</div>
          ))}
          {cells.map((cell, i) => {
            const isToday = cell.current && cell.day === today && year === now.getFullYear() && month === now.getMonth();
            const dow = i % 7;
            return (
              <div
                key={i}
                className={[
                  "mini-cal__day",
                  !cell.current ? "other" : "",
                  isToday ? "today" : "",
                  dow === 0 && cell.current ? "sun" : "",
                  dow === 6 && cell.current ? "sat" : "",
                ].filter(Boolean).join(" ")}
              >
                {cell.day}
              </div>
            );
          })}
        </div>
        <div className="holiday-list">
          {holidayData.map((h, i) => (
            <div key={i} className="holiday-item">
              <span className="holiday-item__date">{h.date}</span>
              <span className="holiday-item__label">{h.label}</span>
              <span className="holiday-item__country">{h.country}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
