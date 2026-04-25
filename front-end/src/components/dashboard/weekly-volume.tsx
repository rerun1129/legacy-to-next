"use client";

import { weeklyVolumeData } from "@/lib/mock-data";

export function WeeklyVolume() {
  const maxTotal = Math.max(...weeklyVolumeData.map((d) => d.total));

  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Weekly Volume (TEU)
        </div>
        <div className="dash-panel__meta">Last 7 days</div>
      </div>
      <div className="wk-chart">
        {weeklyVolumeData.map((d) => {
          const height = (d.total / maxTotal) * 100;
          const fclH = (d.fcl / d.total) * height;
          const lclH = (d.lcl / d.total) * height;
          const bulkH = (d.bulk / d.total) * height;
          return (
            <div key={d.label} className="wk-bar">
              <div className="wk-bar__value">{d.total}</div>
              <div className="wk-bar__stack">
                <div className="wk-bar__seg bulk" style={{ height: `${(bulkH / height) * 100}%` }} />
                <div className="wk-bar__seg lcl"  style={{ height: `${(lclH  / height) * 100}%` }} />
                <div className="wk-bar__seg fcl"  style={{ height: `${(fclH  / height) * 100}%` }} />
              </div>
              <div className="wk-bar__label">{d.label}</div>
            </div>
          );
        })}
      </div>
      <div className="wk-legend">
        <span><i className="fcl" />FCL</span>
        <span><i className="lcl" />LCL</span>
        <span><i className="bulk" />Bulk</span>
      </div>
    </div>
  );
}
