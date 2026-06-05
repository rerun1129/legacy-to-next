"use client";

import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { DateBox, CodeBox } from "@/components/shared/inputs";
import { getSession } from "@/lib/admin-session";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import { authUseCases } from "@/application/auth/use-cases";
import type { MeInfo } from "@/application/auth/ports";
import { todayYyyyMmDd } from "./freight-issue-utils";

export interface FreightIssueHeaderState {
  documentDt: string;
  setDocumentDt: (v: string) => void;
  performanceDt: string;
  setPerformanceDt: (v: string) => void;
  teamCode: string;
  setTeamCode: (v: string) => void;
  operator: string;
  setOperator: (v: string) => void;
  teamAc: ReturnType<typeof useCodeAutocomplete>;
  operatorAc: ReturnType<typeof useCodeAutocomplete>;
  /** 발행 mutation에 전달할 최종 operator 값 (비워두면 null로 발행) */
  operatorForSubmit: string | null;
  /** Team name readOnly 표시 상태 setter */
  setTeamName: (v: string) => void;
  /** Operator name readOnly 표시 상태 setter */
  setOperatorName: (v: string) => void;
  /** Team name readOnly 표시값 (state || 초기 코드 그대로일 때만 초기조회 data 보조) */
  teamNameValue: string;
  /** Operator name readOnly 표시값 (state || 초기 코드 그대로일 때만 초기조회 data 보조) */
  operatorNameValue: string;
}

interface InitialHeaderSeed {
  documentDt?: string;
  performanceDt?: string;
  teamCode?: string;
  operator?: string;
}

export function useFreightIssueHeader(initial?: InitialHeaderSeed): FreightIssueHeaderState {
  // 로그인 세션에서 팀 기본값
  const session = getSession();
  const defaultTeam = session?.attributes?.["team"]?.[0] ?? "";

  // me 캐시에서 username 동기 취득 → operator 초기값(setState-in-effect 회피)
  const qc = useQueryClient();
  const initialUsername = qc.getQueryData<MeInfo>(["auth", "me"])?.username ?? "";

  // seed: initial이 전달되면 우선, 없으면 기본값(issue 모드 동작 동일)
  const seedTeam = initial?.teamCode ?? defaultTeam;
  const seedUser = initial?.operator ?? initialUsername;

  // me 쿼리 유지 — 캐시 워밍 + operatorNameQuery enabled 소스
  const { data: meData } = useQuery({
    queryKey: ["auth", "me"],
    queryFn: () => authUseCases.me(),
    staleTime: 5 * 60 * 1000,
  });

  const [documentDt, setDocumentDt] = useState<string>(
    initial?.documentDt || todayYyyyMmDd,
  );
  const [performanceDt, setPerformanceDt] = useState<string>(
    initial?.performanceDt || todayYyyyMmDd,
  );
  const [teamCode, setTeamCode] = useState<string>(seedTeam);
  const [operator, setOperator] = useState<string>(seedUser);
  const [teamName, setTeamName] = useState<string>("");
  const [operatorName, setOperatorName] = useState<string>("");

  const teamAc = useCodeAutocomplete(CODE_SOURCES.team);
  const operatorAc = useCodeAutocomplete(CODE_SOURCES.user);

  // 초기 팀명 조회 — seedTeam 코드로 autocomplete 결과에서 name 추출
  const teamNameQuery = useQuery({
    queryKey: ["team-name-resolve", seedTeam],
    queryFn: () =>
      CODE_SOURCES.team
        .fetch(seedTeam)
        .then((r) => r.find((x) => x.code === seedTeam)?.name ?? ""),
    enabled: !!seedTeam,
    staleTime: 5 * 60 * 1000,
  });

  // seedUser가 있으면 seedUser 기준, 없으면 me.username 기준
  const resolvedUserForQuery = seedUser || meData?.username;
  // 초기 담당자명 조회 — seedUser 코드로 autocomplete 결과에서 name 추출
  const operatorNameQuery = useQuery({
    queryKey: ["user-name-resolve", resolvedUserForQuery],
    queryFn: () =>
      CODE_SOURCES.user
        .fetch(resolvedUserForQuery!)
        .then((r) => r.find((x) => x.code === resolvedUserForQuery)?.name ?? ""),
    enabled: !!resolvedUserForQuery,
    staleTime: 5 * 60 * 1000,
  });

  // 초기 코드 그대로일 때만 초기조회 이름 보조 표시 — 사용자가 코드를 바꾸거나 비우면 name도 빈칸
  const teamNameValue = teamName || (teamCode === seedTeam ? (teamNameQuery.data ?? "") : "");
  const operatorNameValue =
    operatorName ||
    (seedUser !== "" && operator === seedUser
      ? (operatorNameQuery.data ?? "")
      : "");

  return {
    documentDt,
    setDocumentDt,
    performanceDt,
    setPerformanceDt,
    teamCode,
    setTeamCode,
    operator,
    setOperator,
    teamAc,
    operatorAc,
    // 사용자가 비우면 null로 발행 (me.username 폴백 제거)
    operatorForSubmit: operator || null,
    setTeamName,
    setOperatorName,
    teamNameValue,
    operatorNameValue,
  };
}

interface FreightIssueHeaderProps {
  header: FreightIssueHeaderState;
  ti: ReturnType<typeof useTranslations>;
  /** 전달되면 서류번호 필드를 해당 값으로 표시 (amend 편집 모드에서 실서류번호 표시) */
  documentNo?: string;
}

export function FreightIssueHeader({ header, ti, documentNo }: FreightIssueHeaderProps) {
  const {
    documentDt, setDocumentDt,
    performanceDt, setPerformanceDt,
    teamCode, setTeamCode,
    operator, setOperator,
    teamAc, operatorAc,
    setTeamName, setOperatorName,
    teamNameValue, operatorNameValue,
  } = header;

  return (
    <div style={{ display: "grid", gridTemplateColumns: "repeat(5, minmax(0,1fr))", gap: 8, marginBottom: 12 }}>
      <div className="field">
        <div className="field__label">{ti("documentNo")}</div>
        <div className="field__input">
          <input
            className="input"
            readOnly
            value={documentNo ?? ""}
            placeholder={documentNo == null ? ti("documentNoPlaceholder") : undefined}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("documentDt")}</div>
        <div className="field__input">
          <DateBox
            name="documentDt"
            value={documentDt}
            onChange={(e) => setDocumentDt((e.target as HTMLInputElement).value)}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("performanceDt")}</div>
        <div className="field__input">
          <DateBox
            name="performanceDt"
            value={performanceDt}
            onChange={(e) => setPerformanceDt((e.target as HTMLInputElement).value)}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("team")}</div>
        <div className="field__input">
          <CodeBox
            kind="lcn"
            codeProps={{
              value: teamCode,
              onChange: (e) => {
                setTeamCode(e.target.value);
                // 타이핑/비우기 시 name 초기화 — 선택 시 onSelect가 name을 다시 채움
                setTeamName("");
              },
              placeholder: ti("team"),
            }}
            nameProps={{ value: teamNameValue, readOnly: true }}
            onSearch={teamAc.onSearch}
            suggestions={teamAc.suggestions}
            suggestionsLoading={teamAc.suggestionsLoading}
            onSelect={(it) => {
              setTeamCode(it.code);
              setTeamName(it.name);
            }}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("operator")}</div>
        <div className="field__input">
          <CodeBox
            kind="lcn"
            codeProps={{
              value: operator,
              onChange: (e) => {
                setOperator(e.target.value);
                // 타이핑/비우기 시 name 초기화 — 선택 시 onSelect가 name을 다시 채움
                setOperatorName("");
              },
              placeholder: ti("operator"),
            }}
            nameProps={{ value: operatorNameValue, readOnly: true }}
            onSearch={operatorAc.onSearch}
            suggestions={operatorAc.suggestions}
            suggestionsLoading={operatorAc.suggestionsLoading}
            onSelect={(it) => {
              setOperator(it.code);
              setOperatorName(it.name);
            }}
          />
        </div>
      </div>
    </div>
  );
}
