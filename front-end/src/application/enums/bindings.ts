import { fetchEnum, fetchEnums } from '@/adapter/out/api/enums'
import type { EnumPort } from './ports'

// 어댑터 교체 시 이 파일만 수정
export const enumPort: EnumPort = {
  fetchEnum,
  fetchEnums,
}
