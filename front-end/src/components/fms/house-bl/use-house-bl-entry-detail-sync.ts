import { useRef, useEffect }      from "react";
import { useQuery }               from "@tanstack/react-query";
import type { UseFormReturn }     from "react-hook-form";
import type { MutableRefObject }  from "react";
import { houseBlPort }            from "@/lib/ports";
import { mapHouseBlDetailToForm } from "./map-house-bl-detail";
import type { HouseBlFormValues } from "./house-bl-schema";

export function useHouseBlEntryDetailSync(args: {
  id: number | undefined;
  isEdit: boolean;
  form: UseFormReturn<HouseBlFormValues>;
  didRestoreFromDraftRef: MutableRefObject<boolean>;
}): {
  detail: Awaited<ReturnType<typeof houseBlPort.getById>> | undefined;
  isDetailFetching: boolean;
  detailLoadedRef: MutableRefObject<boolean>;
} {
  const { id, isEdit, form, didRestoreFromDraftRef } = args;

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["house-bl", "detail", id],
    queryFn: () => houseBlPort.getById(id!),
    enabled: isEdit,
    // 다른 화면 이동 후 재진입 시 자동 재조회 차단 — invalidateQueries(mutation 후) 시에는 active query 이므로 refetch 정상 동작
    staleTime: Infinity,
    gcTime: Infinity, // staleTime: Infinity만으로는 gcTime 기본 5분에 막혀 무력화됨 (§6.36)
    refetchOnMount: false,
    // refetch 결과가 직전 cache와 deep equal이어도 새 reference를 발급해
    // useEffect(detail)의 form.reset이 항상 트리거되도록 강제
    structuralSharing: false,
  });

  const detailLoadedRef = useRef<boolean>(false);

  // id 변경 시 form.reset 재트리거를 위해 ref 초기화
  useEffect(() => {
    detailLoadedRef.current = false;
  }, [id]);

  // draft 복원 시 detail로 덮어쓰지 않음 (Master 패턴 정합)
  // detailLoadedRef는 detail 도착 시 즉시 true로 잠가 draft↔detail race에서
  // 두 번째 trigger의 추가 form.reset을 차단한다.
  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    if (didRestoreFromDraftRef.current) return;
    // §6.48 ⑧ — BE 응답 필드 전체를 form에 반영 (3축 동기: BE response ↔ FE domain type ↔ form.reset)
    form.reset(mapHouseBlDetailToForm(detail));
  }, [detail, form, didRestoreFromDraftRef]);

  return { detail, isDetailFetching, detailLoadedRef };
}
