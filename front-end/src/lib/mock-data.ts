// Mock data for FMS frontend

export const kpiData = [
  { label: "Active Shipments", value: "284", delta: "+12 this week", trend: "up", variant: "default" },
  { label: "ETD This Week",    value: "47",  delta: "+8 vs last wk", trend: "up", variant: "ok" },
  { label: "Docs Pending",     value: "23",  delta: "3 overdue",     trend: "down", variant: "warn" },
  { label: "MTD Invoiced",     value: "₩2.4B", delta: "+18% vs LM", trend: "up", variant: "default" },
  { label: "On-Time %",        value: "91.4", delta: "-0.6pp MoM",  trend: "down", variant: "neutral" },
];

export const pipelineData = [
  { label: "Booked",  count: 84, pct: 78, variant: "default" },
  { label: "Docs",    count: 61, pct: 56, variant: "default" },
  { label: "Gate-In", count: 48, pct: 44, variant: "warn"    },
  { label: "Loaded",  count: 52, pct: 48, variant: "ok"      },
  { label: "Sailed",  count: 39, pct: 36, variant: "ok"      },
];

export const weeklyVolumeData = [
  { label: "4/14", fcl: 42, lcl: 18, bulk: 5,  total: 65 },
  { label: "4/15", fcl: 38, lcl: 22, bulk: 3,  total: 63 },
  { label: "4/16", fcl: 51, lcl: 14, bulk: 8,  total: 73 },
  { label: "4/17", fcl: 44, lcl: 20, bulk: 6,  total: 70 },
  { label: "4/18", fcl: 58, lcl: 25, bulk: 4,  total: 87 },
  { label: "4/21", fcl: 35, lcl: 17, bulk: 7,  total: 59 },
  { label: "4/22", fcl: 47, lcl: 21, bulk: 5,  total: 73 },
];

export const timelineData = [
  {
    dow: "MON", date: "21", mo: "APR", today: false,
    items: [
      { tag: "etd", hbl: "HBLKR24041801", route: "KRBSAN → CNSHA", port: "CNSHA" },
      { tag: "cut", hbl: "HBLKR24041734", route: "KRICN → USLAX", port: "USLAX" },
    ],
  },
  {
    dow: "TUE", date: "22", mo: "APR", today: false,
    items: [
      { tag: "etd", hbl: "HBLKR24041845", route: "KRBSAN → JPYOK", port: "JPYOK" },
      { tag: "eta", hbl: "HBLKR24040923", route: "CNNGB → KRICN",  port: "KRICN" },
    ],
  },
  {
    dow: "WED", date: "23", mo: "APR", today: false,
    items: [
      { tag: "etd", hbl: "HBLKR24041901", route: "KRICN → DEHAM", port: "DEHAM" },
    ],
  },
  {
    dow: "THU", date: "24", mo: "APR", today: true,
    items: [
      { tag: "etd", hbl: "HBLKR24041956", route: "KRBSAN → SGSIN", port: "SGSIN" },
      { tag: "eta", hbl: "HBLKR24041102", route: "USLAX → KRICN",  port: "KRICN" },
      { tag: "cut", hbl: "HBLKR24042001", route: "KRINC → VNHPH",  port: "VNHPH" },
    ],
  },
];

export const taskData = [
  { pri: "hi", title: "HBLKR24041801 — B/L Draft 확인 요청", ref: "HBLKR24041801", assignee: "KYS", age: "2h" },
  { pri: "hi", title: "MBLSCO240418 Master B/L 발급 승인 대기", ref: "MBLSCO240418", assignee: "LJY", age: "3h" },
  { pri: "md", title: "HBLKR24041734 S/I 미제출 — 내일 Cut-off", ref: "HBLKR24041734", assignee: "PKH", age: "5h", overdue: false },
  { pri: "md", title: "HBLKR24041623 D/O 발급 요청", ref: "HBLKR24041623", assignee: "KYS", age: "1d" },
  { pri: "lo", title: "4월 MTD 리포트 작성", ref: "-", assignee: "LJY", age: "2d" },
];

export const noticeData = [
  { pinned: true,  cat: "urgent", title: "KRBSAN 터미널 파업 예고 — 4/28-29 선적 지연 가능", date: "04/24" },
  { pinned: true,  cat: "update", title: "CMA CGM 운임 인상 공지 — 5월 1일부터 적용",       date: "04/23" },
  { pinned: false, cat: "event",  title: "[연결] 4월 결산 마감 — 4/30 18:00",               date: "04/23" },
  { pinned: false, cat: "update", title: "중국 노동절 연휴 기간 EDI 처리 일정 안내",          date: "04/22" },
  { pinned: false, cat: "update", title: "항만 혼잡 서차지 개정 — USLAX / USLGB",            date: "04/21" },
];

export const traceData = [
  { state: "done",    label: "Booking Confirmed",       meta: "CMA CGM GRACE / VOY 0412E", time: "04/15 14:22" },
  { state: "done",    label: "S/I Received",            meta: "EDI 315 수신 완료",           time: "04/17 09:08" },
  { state: "done",    label: "B/L Draft Approved",      meta: "담당자: 김영선",              time: "04/18 16:45" },
  { state: "current", label: "Gate-In",                 meta: "KRBSAN ICT 터미널",          time: "진행 중" },
  { state: "future",  label: "On Board",                meta: "ETD: 04/24",                 time: "" },
  { state: "future",  label: "Arrival at POD",          meta: "ETA: 05/08 CNSHA",           time: "" },
];

export const activityData = [
  { initials: "KY", name: "김영선", action: "updated", ref: "HBLKR24041801", detail: "B/L status → Confirmed", time: "10m" },
  { initials: "LJ", name: "이진영", action: "created",  ref: "HBLKR24041956", detail: "New House B/L", time: "28m" },
  { initials: "PK", name: "박경훈", action: "uploaded", ref: "HBLKR24041734", detail: "S/I document", time: "1h" },
  { initials: "KY", name: "김영선", action: "sent",     ref: "MBLSCO240418",  detail: "EDI CUSREP to customs", time: "2h" },
  { initials: "SY", name: "서유나", action: "approved", ref: "HBLKR24041623", detail: "D/O issuance", time: "3h" },
];

export const fxData = [
  { pair: "USD/KRW", name: "US Dollar",          rate: "1,376.50", chg: "+4.20",  dir: "up"   },
  { pair: "CNY/KRW", name: "Chinese Yuan",        rate: "189.34",  chg: "-0.82",  dir: "down" },
  { pair: "JPY/KRW", name: "Japanese Yen",        rate: "9.18",    chg: "+0.12",  dir: "up"   },
  { pair: "EUR/KRW", name: "Euro",               rate: "1,564.70", chg: "-6.30",  dir: "down" },
  { pair: "VND/KRW", name: "Vietnamese Dong",    rate: "0.054",   chg: "+0.001", dir: "up"   },
];

export const shortcutData = [
  { label: "New House B/L", hint: "⌘N", href: "/fms/house-bl/new" },
  { label: "New Master B/L", hint: "⌘M", href: "/fms/master-bl/new" },
  { label: "Cargo Trace", hint: "⌘T", href: "/dashboard" },
  { label: "B/L Print", hint: "⌘P", href: "#" },
  { label: "Invoice", hint: "⌘I", href: "#" },
  { label: "EDMS", hint: "⌘E", href: "#" },
];

export const holidayData = [
  { date: "04/25", label: "Việt Nam Liberation Day", country: "VN" },
  { date: "05/01", label: "Labour Day",              country: "KR" },
  { date: "05/05", label: "Children's Day",          country: "KR" },
  { date: "05/06", label: "Buddha's Birthday (ALT)", country: "KR" },
];

// House B/L list mock rows
export const houseBLRows = [
  { no: 1,  hbl: "HBLKR24041956", expImp: "EXP", docStatus: "ok",    mbl: "COSCO2404195",  sType: "FCL", lType: "CY/CY", etd: "04/24", eta: "05/08", regDate: "04/20", pol: "KRBSAN", pod: "CNSHA", vessel: "COSCO EXCELLENCE", voyage: "0412E", shipper: "한진무역(주)", consignee: "SHANGHAI TRADING CO.", },
  { no: 2,  hbl: "HBLKR24041901", expImp: "EXP", docStatus: "inprog", mbl: "HAPAG0418011", sType: "FCL", lType: "CY/CY", etd: "04/23", eta: "05/12", regDate: "04/19", pol: "KRICN", pod: "DEHAM", vessel: "HAPAG EXPRESS", voyage: "0419W", shipper: "삼성전자(주)", consignee: "SAMSUNG EUROPE GMBH", },
  { no: 3,  hbl: "HBLKR24041845", expImp: "EXP", docStatus: "ok",    mbl: "ONL240418045", sType: "LCL", lType: "CFS/CFS", etd: "04/22", eta: "04/28", regDate: "04/18", pol: "KRBSAN", pod: "JPYOK", vessel: "ONE CRANE",      voyage: "0041N", shipper: "현대상사",      consignee: "NIPPON TRADING K.K.", },
  { no: 4,  hbl: "HBLKR24041801", expImp: "EXP", docStatus: "draft", mbl: "COSCO2404180", sType: "FCL", lType: "CY/CY", etd: "04/21", eta: "05/05", regDate: "04/17", pol: "KRBSAN", pod: "CNSHA", vessel: "COSCO HARMONY",  voyage: "0411E", shipper: "한국섬유(주)",  consignee: "GUANGZHOU IMPORT CO.", },
  { no: 5,  hbl: "HBLKR24041734", expImp: "EXP", docStatus: "draft", mbl: "MSC240417034", sType: "FCL", lType: "CY/CY", etd: "04/21", eta: "05/18", regDate: "04/17", pol: "KRICN", pod: "USLAX", vessel: "MSC ANNA",       voyage: "AX401", shipper: "LG전자(주)",    consignee: "LG ELECTRONICS USA INC.", },
  { no: 6,  hbl: "HBLKR24041623", expImp: "IMP", docStatus: "ok",    mbl: "EMC240416023", sType: "FCL", lType: "CY/CY", eta: "04/20", etd: "04/02", regDate: "04/16", pol: "CNNGB", pod: "KRICN", vessel: "EVER CHARTER",  voyage: "0161N", shipper: "NINGBO EXPORTS",  consignee: "한국수입상사", },
  { no: 7,  hbl: "HBLKR24041589", expImp: "EXP", docStatus: "ok",    mbl: "YML240415089", sType: "FCL", lType: "CY/CY", etd: "04/20", eta: "05/04", regDate: "04/15", pol: "KRBSAN", pod: "SGSIN", vessel: "YM WARRANTY",   voyage: "045E",  shipper: "포스코인터내셔널", consignee: "SINGAPORE STEEL PTE.", },
  { no: 8,  hbl: "HBLKR24041512", expImp: "IMP", docStatus: "inprog", mbl: "PIL240415012", sType: "LCL", lType: "CFS/CFS", eta: "04/19", etd: "04/05", regDate: "04/14", pol: "VNHPH", pod: "KRICN", vessel: "PIL MERCURY",   voyage: "0151N", shipper: "HAIPHONG EXPORTS", consignee: "베트남수입사", },
];

// Sidebar navigation structure
export const navItems = [
  {
    group: "FMS",
    items: [
      { label: "Dashboard",      href: "/dashboard",          icon: "layout-dashboard" },
      { label: "House B/L",      href: "/fms/house-bl",       icon: "file-text" },
      { label: "Master B/L",     href: "/fms/master-bl",      icon: "layers" },
      { label: "Truck B/L",      href: "/fms/truck-bl",       icon: "truck" },
      { label: "Non B/L",        href: "/fms/non-bl",         icon: "package" },
    ],
  },
  {
    group: "BMS",
    items: [
      { label: "Invoice",        href: "/bms/invoice",        icon: "receipt" },
      { label: "Payment",        href: "/bms/payment",        icon: "credit-card" },
    ],
  },
  {
    group: "SETTINGS",
    items: [
      { label: "Masters",        href: "/masters",            icon: "database" },
      { label: "Admin",          href: "/admin",              icon: "settings" },
    ],
  },
];
