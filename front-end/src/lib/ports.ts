import { API_AIR_HOUSE_PORT } from '@/adapter/out/api/air-house';
import { API_HOUSE_BL_PORT } from '@/adapter/out/api/house-bl';
import { API_MASTER_BL_PORT } from '@/adapter/out/api/master-bl';
import { API_NON_BL_PORT } from '@/adapter/out/api/non-bl';
import { API_SWITCH_BL_PORT } from '@/adapter/out/api/switch-bl';
import { API_TRUCK_BL_PORT } from '@/adapter/out/api/truck-bl';
import { mockHouseBlPort } from '@/adapter/out/mock/house-bl';
import { mockMasterBlPort } from '@/adapter/out/mock/master-bl';
import { mockSwitchBlPort } from '@/adapter/out/mock/switch-bl';

const useMock = process.env.NEXT_PUBLIC_USE_MOCK === 'true';

export const airHousePort = API_AIR_HOUSE_PORT;
export const houseBlPort = useMock ? mockHouseBlPort : API_HOUSE_BL_PORT;
export const masterBlPort = useMock ? mockMasterBlPort : API_MASTER_BL_PORT;
export const nonBlPort = API_NON_BL_PORT;
export const switchBlPort = useMock ? mockSwitchBlPort : API_SWITCH_BL_PORT;
export const truckBlPort = API_TRUCK_BL_PORT;
