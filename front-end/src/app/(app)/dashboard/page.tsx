import { KpiStrip } from "@/components/dashboard/kpi-strip";
import { ShipmentPipeline } from "@/components/dashboard/shipment-pipeline";
import { WeeklyVolume } from "@/components/dashboard/weekly-volume";
import { CargoTrace } from "@/components/dashboard/cargo-trace";
import { Timeline } from "@/components/dashboard/timeline";
import { TaskList } from "@/components/dashboard/task-list";
import { Notices } from "@/components/dashboard/notices";
import { HolidayCalendar } from "@/components/dashboard/holiday-calendar";
import { ExchangeRates } from "@/components/dashboard/exchange-rates";
import { ActivityFeed } from "@/components/dashboard/activity-feed";
import { Shortcuts } from "@/components/dashboard/shortcuts";

export default function DashboardPage() {
  return (
    <div className="app__main--dash">
      <KpiStrip />

      {/* Row 1: Pipeline / Weekly Volume / Cargo Trace */}
      <div className="dash-row dash-row--top">
        <ShipmentPipeline />
        <WeeklyVolume />
        <CargoTrace />
      </div>

      {/* Row 2: Timeline / Tasks */}
      <div className="dash-row dash-row--mid">
        <Timeline />
        <TaskList />
      </div>

      {/* Row 3: Notices / Holiday Calendar / Exchange Rates + Shortcuts + Activity */}
      <div className="dash-row dash-row--bot">
        <Notices />
        <HolidayCalendar />
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <ExchangeRates />
          <Shortcuts />
        </div>
      </div>

      {/* Row 4: Activity Feed */}
      <div className="dash-row" style={{ gridTemplateColumns: "1fr" }}>
        <ActivityFeed />
      </div>
    </div>
  );
}
