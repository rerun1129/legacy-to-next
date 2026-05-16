import { useRef, useEffect }    from "react";
import { useQuery }             from "@tanstack/react-query";
import type { UseFormReturn }   from "react-hook-form";
import type { MutableRefObject } from "react";
import { masterBlPort }         from "@/lib/ports";
import { mapMasterBlDetailToForm } from "./map-master-bl-detail";
import type { MasterBlFormValues } from "./master-bl-schema";

export function useMasterBlEntryDetailSync(args: {
  id: number | undefined;
  isEdit: boolean;
  form: UseFormReturn<MasterBlFormValues>;
  didRestoreFromDraftRef: MutableRefObject<boolean>;
}): {
  detail: Awaited<ReturnType<typeof masterBlPort.getById>> | undefined;
  isDetailFetching: boolean;
  detailLoadedRef: MutableRefObject<boolean>;
} {
  const { id, isEdit, form, didRestoreFromDraftRef } = args;

  const { data: detail, isFetching: isDetailFetching } = useQuery({
    queryKey: ["master-bl", "detail", id],
    queryFn: () => masterBlPort.getById(id!),
    enabled: isEdit,
    staleTime: Infinity,
    gcTime: Infinity,
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

  // §6.49 ⑨ — draft 복원 시 detail로 덮어쓰지 않음 (House 패턴 정합)
  // detailLoadedRef는 detail 도착 시 즉시 true로 잠가 draft↔detail race에서
  // 두 번째 trigger의 추가 form.reset을 차단한다 (House 패턴 정합).
  useEffect(() => {
    if (detailLoadedRef.current) return;
    if (!detail) return;
    detailLoadedRef.current = true;
    if (didRestoreFromDraftRef.current) return;
    form.reset(mapMasterBlDetailToForm(detail));
  }, [detail, form, didRestoreFromDraftRef]);

  return { detail, isDetailFetching, detailLoadedRef };
}
