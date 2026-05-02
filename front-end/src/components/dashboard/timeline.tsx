import { TIMELINE_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function Timeline() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Upcoming ETD / ETA
        </div>
        <div className="dash-panel__meta">Next 7 days</div>
      </div>
      <div className="timeline">
        {TIMELINE_DATA.map((day) => (
          <div key={day.date} className={`tl-day${day.today ? " today" : ""}`}>
            <div className="tl-day__when">
              <div className="tl-day__dow">{day.dow}</div>
              <div className="tl-day__date">{day.date}</div>
              <div className="tl-day__mo">{day.mo}</div>
            </div>
            <div className="tl-items">
              {day.items.map((item) => (
                <div key={item.hbl} className="tl-item">
                  <div className={`tl-item__tag ${item.tag}`}>{item.tag.toUpperCase()}</div>
                  <div>
                    <span className="tl-item__hbl">{item.hbl}</span>
                    <span className="tl-item__route">{item.route}</span>
                  </div>
                  <div className="tl-item__port">{item.port}</div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
