"use client";

import { useState, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";
import { DateBox, CodeBox } from "@/components/shared/inputs";
import { getSession } from "@/lib/admin-session";
import { useCodeAutocomplete } from "@/lib/use-code-autocomplete";
import { CODE_SOURCES } from "@/lib/autocomplete-sources";
import { authUseCases } from "@/application/auth/use-cases";
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
  teamValidRef: React.RefObject<string>;
  operatorValidRef: React.RefObject<string>;
  meData: { username?: string } | undefined;
  /** 발행 mutation에 전달할 최종 operator 값 (빈 경우 me.username 폴백) */
  operatorForSubmit: string | null;
  /** CodeBox value prop용 표시 값 */
  operatorValue: string;
}

export function useFreightIssueHeader(): FreightIssueHeaderState {
  // 로그인 세션에서 팀 기본값
  const session = getSession();
  const defaultTeam = session?.attributes?.["team"]?.[0] ?? "";

  // me 조회 — staleTime 캐시 히트 시 모달 오픈과 동시에 동기적으로 사용 가능.
  // operator 기본값(PRD §3): 로그인 사용자의 username.
  const { data: meData } = useQuery({
    queryKey: ["auth", "me"],
    queryFn: () => authUseCases.me(),
    staleTime: 5 * 60 * 1000,
  });

  const [documentDt, setDocumentDt] = useState<string>(todayYyyyMmDd);
  const [performanceDt, setPerformanceDt] = useState<string>(todayYyyyMmDd);
  const [teamCode, setTeamCode] = useState<string>(defaultTeam);
  // operator는 빈 문자열로 초기화 — 표시·제출 시 meData.username 폴백(setState-in-effect 회피)
  const [operator, setOperator] = useState<string>("");

  const teamAc = useCodeAutocomplete(CODE_SOURCES.team);
  const operatorAc = useCodeAutocomplete(CODE_SOURCES.user);

  // 직전 유효값 ref — invalid blur 시 선택 전 입력을 버리고 복원
  // team 초기 유효값: 세션에서 받은 defaultTeam
  const teamValidRef = useRef<string>(defaultTeam);
  // operator 초기 유효값: me 비동기 응답 전이므로 빈 문자열, blur 시 me.username 폴백
  const operatorValidRef = useRef<string>("");

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
    teamValidRef,
    operatorValidRef,
    meData,
    // 사용자가 직접 입력하지 않은 경우 me.username 폴백(PRD §3)
    operatorForSubmit: operator || meData?.username || null,
    operatorValue: operator || (meData?.username ?? ""),
  };
}

interface FreightIssueHeaderProps {
  header: FreightIssueHeaderState;
  ti: ReturnType<typeof useTranslations>;
}

export function FreightIssueHeader({ header, ti }: FreightIssueHeaderProps) {
  const {
    documentDt, setDocumentDt,
    performanceDt, setPerformanceDt,
    teamCode, setTeamCode,
    setOperator,
    teamAc, operatorAc,
    teamValidRef, operatorValidRef,
    meData,
    operatorValue,
  } = header;

  return (
    <div style={{ display: "grid", gridTemplateColumns: "repeat(5, 1fr)", gap: 8, marginBottom: 12 }}>
      <div className="field">
        <div className="field__label">{ti("documentNo")}</div>
        <div className="field__input">
          <input className="input" readOnly value="" placeholder={ti("documentNoPlaceholder")} />
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
            kind="code-only"
            clearInvalidOnBlur={false}
            codeProps={{
              value: teamCode,
              onChange: (e) => setTeamCode(e.target.value),
              // 미선택 blur 시 직전 유효값으로 복원(자유 입력 차단)
              onBlur: () => setTeamCode(teamValidRef.current),
              placeholder: ti("team"),
            }}
            onSearch={teamAc.onSearch}
            suggestions={teamAc.suggestions}
            suggestionsLoading={teamAc.suggestionsLoading}
            onSelect={(it) => {
              setTeamCode(it.code);
              teamValidRef.current = it.code;
            }}
          />
        </div>
      </div>
      <div className="field">
        <div className="field__label">{ti("operator")}</div>
        <div className="field__input">
          <CodeBox
            kind="code-only"
            clearInvalidOnBlur={false}
            codeProps={{
              value: operatorValue,
              onChange: (e) => setOperator(e.target.value),
              // 미선택 blur 시 직전 유효값으로 복원, me.username 폴백(세션 초기값 보장)
              onBlur: () => setOperator(operatorValidRef.current || meData?.username || ""),
              placeholder: ti("operator"),
            }}
            onSearch={operatorAc.onSearch}
            suggestions={operatorAc.suggestions}
            suggestionsLoading={operatorAc.suggestionsLoading}
            onSelect={(it) => {
              setOperator(it.code);
              operatorValidRef.current = it.code;
            }}
          />
        </div>
      </div>
    </div>
  );
}
