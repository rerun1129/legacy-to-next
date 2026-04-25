import { activityData } from "@/adapter/out/mock/mock-data";

export function ActivityFeed() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Recent Activity
        </div>
      </div>
      <div className="activity">
        {activityData.map((a, i) => (
          <div key={i} className="activity__item">
            <div className="activity__avatar">{a.initials}</div>
            <div className="activity__text">
              <strong>{a.name}</strong> {a.action}{" "}
              <span className="mono">{a.ref}</span> — {a.detail}
            </div>
            <div className="activity__time">{a.time}</div>
          </div>
        ))}
      </div>
    </div>
  );
}
