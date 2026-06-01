// 버튼 코드 action suffix → 공통(common.*) 메시지 키 매핑.
// ActionButton 라벨 해석 우선순위: i18n(키 존재 시) > DB 권한 라벨 > children.
// 긴/구체 suffix가 먼저 오도록 정렬 — endsWith("SAVE")가
// SAVE / DETAIL_SAVE / VALUE_SAVE / SUBSCRIPTION_SAVE를 모두 포함.
const BUTTON_LABEL_RULES: ReadonlyArray<readonly [string, string]> = [
  ["_SEARCH_BL", "common.search"],
  ["SAVE",       "common.save"],
  ["_UPDATE",    "common.save"],
  ["_RESET",     "common.reset"],
  ["_SEARCH",    "common.search"],
  ["_DELETE",    "common.delete"],
];

/**
 * 버튼 코드 suffix로 common.* 메시지 키를 찾는다.
 * CREATE / CHANGE_BL_NO / PRINT / SWITCH_BL 등 매핑이 없으면 null 반환 →
 * 호출측에서 DB 권한 라벨로 fallback.
 */
export function resolveButtonLabelKey(buttonCode: string): string | null {
  for (const [suffix, key] of BUTTON_LABEL_RULES) {
    if (buttonCode.endsWith(suffix)) return key;
  }
  return null;
}
