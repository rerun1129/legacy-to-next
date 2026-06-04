// 서류 발행 모달 공유 타입
// 상위 그리드에서 체크 후 발행 버튼 onClick 시 고정되는 스냅샷.
// 이후 그리드 선택 변경과 독립적으로 모달이 동작하도록 분리.

export interface SelectedFreightLine {
  freightLineId:    number;
  customerCode:     string;
  customerName:     string;
  financialDocType: string;
  currency:         string;
  settleAmount:     number | null;
  localAmount:      number | null;
  vat:              number | null;
  usdAmount:        number | null;
  performanceDt:    string;
  // 외부 Freight 탭 그리드와 동일 컬럼 구성을 위한 추가 필드
  freightCode:      string;
  freightName:      string;
  exchangeRate:     number | null;
  per:              string;
  qty:              number | null;
  price:            number | null;
  taxType:          string;
}

export interface FreightIssueModalProps {
  isOpen: boolean;
  onClose: () => void;
  blType: string;
  blId: string | number;
  freightType: "SELLING" | "BUYING";
  /** 상위 그리드에서 체크 후 전달되는 발행 대상 스냅샷 */
  selectedLines: SelectedFreightLine[];
  /** 발행 성공 후 상위 그리드 체크박스 선택 해제 콜백 */
  onIssueSuccess: () => void;
}
