import { timelineData } from "@/adapter/out/mock/mock-data";

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
        {timelineData.map((day) => (
          <div key={day.date} className={`tl-day${day.today ? " today" : ""}`}>
            <div className="tl-day__when">
              <div className="tl-day__dow">{day.dow}</div>
              <div className="tl-day__date">{day.date}</div>
              <div className="tl-day__mo">{day.mo}</div>
            </div>
            <div className="tl-items">
              {day.items.map((item, i) => (
                <div key={i} className="tl-item">
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
