import { API_HOUSE_BL_PORT } from '@/adapter/out/api/house-bl';
import { API_MASTER_BL_PORT } from '@/adapter/out/api/master-bl';
import { mockHouseBlPort } from '@/adapter/out/mock/house-bl';
import { mockMasterBlPort } from '@/adapter/out/mock/master-bl';

const useMock = process.env.NEXT_PUBLIC_USE_MOCK === 'true';

export const houseBlPort = useMock ? mockHouseBlPort : API_HOUSE_BL_PORT;
export const masterBlPort = useMock ? mockMasterBlPort : API_MASTER_BL_PORT;
