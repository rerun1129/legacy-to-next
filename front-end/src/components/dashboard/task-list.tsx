import { taskData } from "@/lib/mock-data";

export function TaskList() {
  return (
    <div className="dash-panel">
      <div className="dash-panel__head">
        <div className="dash-panel__title">
          <div className="dash-panel__title-accent" />
          My Tasks
        </div>
        <div className="dash-panel__meta" style={{ marginLeft: "auto", fontSize: 10.5 }}>
          {taskData.length} items
        </div>
      </div>
      <div className="dash-panel__body dash-panel__body--flush">
        <div className="task-list" style={{ padding: "4px 14px" }}>
          {taskData.map((task, i) => (
            <div key={i} className="task">
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
