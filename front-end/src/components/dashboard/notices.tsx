import { noticeData } from "@/lib/mock-data";

const catClass: Record<string, string> = { urgent: "urgent", update: "update", event: "event" };

export function Notices() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          Notices
        </div>
      </div>
      <div className="dash-panel__body--flush notice-list">
        <table>
          <thead>
            <tr>
              <th>Category</th>
              <th className="title">Title</th>
              <th>Date</th>
            </tr>
          </thead>
          <tbody>
            {noticeData.map((n, i) => (
              <tr key={i}>
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
