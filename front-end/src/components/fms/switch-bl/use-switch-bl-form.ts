"use client";

import { useEffect } from "react";
import { useForm, type UseFormReturn } from "react-hook-form";
import { useMutation, useQuery, type UseMutationResult } from "@tanstack/react-query";
import { switchBlPort } from "@/lib/ports";
import { toast } from "@/lib/toast-store";
import { confirm } from "@/components/confirm";
import type { SwitchBl } from "@/domain/switch-bl";
import type { SwitchBlFormValues, InitialFromHouseBl } from "./switch-bl-modal";

interface UseSwitchBlFormArgs {
  houseBlId: number;
  initialFromHouseBl: InitialFromHouseBl;
  onClose: () => void;
}

interface UseSwitchBlFormResult {
  form: UseFormReturn<SwitchBlFormValues>;
  isLoading: boolean;
  isUpdateMode: boolean;
  saveMutation: UseMutationResult<SwitchBl, Error, SwitchBlFormValues>;
  deleteMutation: UseMutationResult<void, Error, void>;
  refetch: () => void;
  handleSubmit: (values: SwitchBlFormValues) => Promise<void>;
  handleDelete: () => Promise<void>;
}

export function useSwitchBlForm({
  houseBlId,
  initialFromHouseBl,
  onClose,
}: UseSwitchBlFormArgs): UseSwitchBlFormResult {
  const form = useForm<SwitchBlFormValues>({
    defaultValues: {
      switchBlNo: "",
      shipperCode: "",
      shipperAddress: "",
      consigneeCode: "",
      consigneeAddress: "",
      notifyCode: "",
      notifyAddress: "",
      marks: "",
      natureQuantity: "",
      incoterms: "",
      blType: "",
    },
  });

  // 모달 unmount(닫기) 시 캐시를 즉시 폐기 → 재오픈 시 항상 서버에서 새로 조회
  const { data: existing, isLoading, refetch } = useQuery({
    queryKey: ["switch-bl", "byHouseBl", houseBlId],
    queryFn: () => switchBlPort.getByHouseBlId(houseBlId),
    gcTime: 0,
  });

  // 서버 데이터 로드 시 폼 reset
  // initialFromHouseBl은 모달 mount 시점 한 번만 바인딩되므로 deps 제외
  useEffect(() => {
    if (existing) {
      form.reset({
        switchBlNo: existing.switchBlNo ?? "",
        shipperCode: existing.shipperCode ?? "",
        shipperAddress: existing.shipperAddress ?? "",
        consigneeCode: existing.consigneeCode ?? "",
        consigneeAddress: existing.consigneeAddress ?? "",
        notifyCode: existing.notifyCode ?? "",
        notifyAddress: existing.notifyAddress ?? "",
        marks: existing.description?.marks ?? "",
        natureQuantity: existing.description?.natureQuantity ?? "",
        incoterms: existing.incoterms ?? "",
        blType: existing.blType ?? "",
      });
    } else if (existing === null) {
      // 미존재 CREATE 모드: Switch B/L No만 비우고 나머지는 House B/L 폼 값으로 초기 바인딩
      form.reset({ switchBlNo: "", ...initialFromHouseBl });
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [existing, form]);

  const isUpdateMode = Boolean(existing);

  const saveMutation = useMutation({
    mutationFn: (values: SwitchBlFormValues) => {
      const body = {
        houseBlId,
        switchBlNo: values.switchBlNo || undefined,
        blType:     values.blType     || undefined,
        incoterms:  values.incoterms  || undefined,
        shipperCode: values.shipperCode,
        shipperAddress: values.shipperAddress || undefined,
        consigneeCode: values.consigneeCode || undefined,
        consigneeAddress: values.consigneeAddress || undefined,
        notifyCode: values.notifyCode || undefined,
        notifyAddress: values.notifyAddress || undefined,
        description: (values.marks || values.natureQuantity)
          ? { marks: values.marks || undefined, natureQuantity: values.natureQuantity || undefined }
          : undefined,
      };
      return isUpdateMode
        ? switchBlPort.update(existing!.id, body)
        : switchBlPort.create(body);
    },
    onSuccess: () => {
      toast.success("Switch B/L saved.");
      onClose();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => switchBlPort.delete(existing!.id),
    onSuccess: () => {
      toast.success("Switch B/L deleted.");
      onClose();
    },
  });

  async function handleSubmit(values: SwitchBlFormValues) {
    const ok = await confirm({
      title: "저장하시겠습니까?",
      variant: "default",
    });
    if (!ok) return;
    saveMutation.mutate(values);
  }

  async function handleDelete() {
    const ok = await confirm({
      variant: "destructive",
      title: "삭제하시겠습니까?",
      confirmText: "삭제",
      description: "삭제된 데이터는 복구할 수 없습니다.",
    });
    if (!ok) return;
    deleteMutation.mutate();
  }

  return {
    form,
    isLoading,
    isUpdateMode,
    saveMutation,
    deleteMutation,
    refetch,
    handleSubmit,
    handleDelete,
  };
}
