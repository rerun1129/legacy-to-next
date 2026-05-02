import { TASK_DATA } from "@/lib/mock-data";
// TODO: 후속 작업 — 백엔드 미구현 (stub 유지)

export function TaskList() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          My Tasks
        </div>
        <div className="dash-panel__meta" style={{ marginLeft: "auto", fontSize: 10.5 }}>
          {TASK_DATA.length} items
        </div>
      </div>
      <div className="dash-panel__body dash-panel__body--flush">
        <div className="task-list" style={{ padding: "4px 14px" }}>
          {TASK_DATA.map((task) => (
            <div key={task.ref} className="task">
              <div className={`task__pri ${task.pri}`}>{task.pri.toUpperCase()}</div>
              <div className="task__body">
                <div className="task__title">{task.title}</div>
                <div className="task__meta">
                  <span className="mono">{task.ref}</span>
                  <span>{task.assignee}</span>
                </div>
              </div>
              <div className={`task__age${task.overdue ? " overdue" : ""}`}>{task.age}</div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
