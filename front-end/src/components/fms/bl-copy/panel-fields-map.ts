/**
 * @deprecated 직접 import 진입점.
 * 내부 구현은 panel-fields-map/ 디렉토리로 분리됨.
 * 기존 import 경로("./panel-fields-map") 호환을 위해 re-export 유지.
 */
export type { PanelKey, PanelFieldsMap } from "./panel-fields-map/types";
export { getPanelFieldsMap } from "./panel-fields-map/index";
