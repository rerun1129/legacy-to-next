import type { UseFormReturn } from 'react-hook-form';
import type { BLVariantConfig } from '@/lib/bl-variants';
import type { JobDiv, Bound } from '@/domain/house-bl';
import { houseBlPort } from '@/lib/ports';
import type { HouseBlFormValues } from './house-bl-schema';
import { createEmptyHouseBlFormValues } from './house-bl-defaults';

export function useSearchBl(form: UseFormReturn<HouseBlFormValues>, variant: BLVariantConfig) {
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
        return houseBlPort.getById(rows[0].id).then((detail) => {
          form.reset({
            ...createEmptyHouseBlFormValues(),
            hbl:    detail.hblNo ?? '',
            mbl:    detail.masterBlId != null ? String(detail.masterBlId) : '',
            sType:  detail.shipmentType ?? '',
            lType:  detail.blType ?? '',
            etd:    detail.etd ?? '',
            eta:    detail.eta ?? '',
            pol:    detail.polCode ?? '',
            pod:    detail.podCode ?? '',
            settle: detail.freightTerm ?? '',
            expImp: detail.bound,
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
