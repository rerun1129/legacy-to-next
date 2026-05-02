import type { HouseBlPort } from './ports';
import { API_HOUSE_BL_PORT } from '@/adapter/out/api/house-bl';

// 인바운드(컴포넌트)가 참조하는 포트 인스턴스 — 어댑터 교체 시 이 파일만 수정
export const houseBlPort: HouseBlPort = API_HOUSE_BL_PORT;
