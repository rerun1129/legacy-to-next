package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * freight_line basis 실적일자 covered distinct count + page-select용 sidecar.
 * PmsBlMartDocument.lines[]에서 ETL이 (blKey,pd)로 dedup 파생(Phase 1b).
 * 컬렉션: pms_perfdt_entry.
 */
@Document("pms_perfdt_entry")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PmsPerfDtEntryDocument {

    /** blKey + "#" + pd 복합 키. */
    @Id
    private String id;

    /** "HOUSE#123" (= PmsBlMartDocument.id). */
    private String blKey;

    /** 정렬 키(숫자). */
    private Long blId;

    /** 정렬 tie-break. */
    private String blType;

    /** performance_dt (yyyyMMdd 스칼라). */
    private String pd;

    /** 이 (blKey,pd)에 freight_line 존재(항상 true). */
    private boolean hasFreightInput;

    /** 이 날짜에 tax_no IS NOT NULL 라인 존재. */
    private boolean hasTaxIssued;

    /** 이 날짜에 slip_no IS NOT NULL 라인 존재. */
    private boolean hasSlipIssued;

    /** 이 (blKey,pd)에 존재하는 financial_doc_type 집합(covered dim용). */
    private List<String> fdcTypes;

    // ── B/L 레벨 필터 필드(residual 필터용 비정규화. 전부 PmsBlMartDocument에서 복사) ──
    private String jobDiv;
    private String bound;
    private String houseBlNo;
    private String masterBlNo;
    private String actualCustomerCode;
    private String settlePartnerCode;
    private String linerCode;
    private String polCode;
    private String podCode;
    private String salesManCode;
    private String incoterms;
    private String salesClass;
    private String houseTeamCode;
}
