package com.freightos.bms.application.financialdocument.port.in;

import com.freightos.bms.application.financialdocument.GroupResult;
import com.freightos.bms.application.financialdocument.command.ApplyGroupingCommand;

/**
 * 금융 서류 그룹 부여/해제 인바운드 포트.
 */
public interface FinancialDocumentGroupUseCase {

    /**
     * 선택된 서류들에 그룹 번호를 부여하거나 해제한다.
     * groupedDocumentIds에 포함된 서류는 그룹 부여, scope에서 제외된 서류는 해제.
     */
    GroupResult applyGrouping(ApplyGroupingCommand cmd);
}
