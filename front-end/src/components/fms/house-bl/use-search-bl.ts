import type { UseFormReturn } from 'react-hook-form';
import type { BLVariantConfig } from '@/lib/bl-variants';
import type { JobDiv, Bound } from '@/domain/house-bl';
import { useQueryClient } from '@tanstack/react-query';
import { useBLDraftStore } from '@/lib/use-bl-draft-store';
import { houseBlPort } from '@/lib/ports';
import type { HouseBlFormValues } from './house-bl-schema';
import { createEmptyHouseBlFormValues } from './house-bl-defaults';

export function useSearchBl(form: UseFormReturn<HouseBlFormValues>, variant: BLVariantConfig) {
  const queryClient = useQueryClient();
  const clearDraft = useBLDraftStore((s) => s.clearDraft);

  function handleSearchBl() {
    const blNo = form.getValues('hbl');
    if (!blNo?.trim()) return;

    houseBlPort
      .list({
        jobDiv: variant.mode as JobDiv,
        bound: variant.direction as Bound,
        hblNo: blNo.trim(),
      })
      .then((rows) => {
        if (rows.length === 0) {
          alert('해당 B/L을 찾을 수 없습니다.');
          return;
        }
        // 프레시 조회: stale 캐시·draft 제거
        queryClient.invalidateQueries({ queryKey: ['house-bl', 'detail', rows[0].id] });
        clearDraft(`house:${variant.key}:${rows[0].id}`);
        return houseBlPort.getById(rows[0].id).then((detail) => {
          // §6.48 ⑧ — house-bl-entry.tsx form.reset과 동일한 풀 매핑 유지
          form.reset({
            ...createEmptyHouseBlFormValues(),
            hbl:         detail.hblNo ?? '',
            mbl:         detail.masterBlId != null ? String(detail.masterBlId) : '',
            sType:       detail.shipmentType ?? '',
            lType:       detail.loadType ?? '',
            etd:         detail.etd ?? '',
            eta:         detail.eta ?? '',
            pol:         detail.polCode ?? '',
            pod:         detail.podCode ?? '',
            settle:      (detail.freightTerm ?? '') as '' | 'PREPAID' | 'COLLECT',
            expImp:      detail.bound,
            shipperCode:        detail.shipperCode       ?? '',
            shipperAddress:     detail.shipperAddress    ?? '',
            consigneeCode:      detail.consigneeCode     ?? '',
            consigneeAddress:   detail.consigneeAddress  ?? '',
            notifyCode:         detail.notifyCode        ?? '',
            notifyAddress:      detail.notifyAddress     ?? '',
            docPartnerCode:     detail.docPartnerCode    ?? '',
            docPartnerAddress:  detail.docPartnerAddress ?? '',
            pkgQty:          detail.pkgQty    != null ? String(detail.pkgQty)    : '',
            pkgUnit:         detail.pkgUnit   ?? '',
            weightUnit:      detail.weightUnit ?? '',
            grossWeightKg:   detail.grossWeightKg != null ? String(detail.grossWeightKg) : '',
            cbm:             detail.cbm        != null ? String(detail.cbm)        : '',
            volumeWeightKg:  detail.volumeWeightKg != null ? String(detail.volumeWeightKg) : '',
            actualCustomerCode: detail.actualCustomerCode ?? '',
            operatorCode:        detail.operatorCode  ?? '',
            teamCode:            detail.teamCode      ?? '',
            salesManCode:        detail.salesManCode  ?? '',
            linerCode:  detail.linerCode  ?? '',
            linerName:  detail.linerName  ?? '',
            vesselName: detail.vesselName ?? '',
            voyNo:      detail.voyageNo   ?? '',
            remark:     detail.remark     ?? '',
          });
        });
      })
      .catch((err: unknown) => {
        const message = err instanceof Error ? err.message : String(err);
        alert(`B/L 조회 중 오류가 발생했습니다: ${message}`);
      });
  }

  return { handleSearchBl };
}
