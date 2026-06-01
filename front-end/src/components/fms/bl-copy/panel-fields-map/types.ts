export type PanelKey = string;

/** panelKey → 해당 패널이 다루는 폼 필드 경로 배열 */
export type PanelFieldsMap = Record<PanelKey, string[]>;
