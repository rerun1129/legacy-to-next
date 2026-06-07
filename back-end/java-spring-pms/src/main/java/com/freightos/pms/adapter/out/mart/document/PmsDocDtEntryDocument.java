package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * document basis 실적일자/서류일자 covered count + page-select용 sidecar.
 * PmsBlMartDocument.docs[]에서 ETL이 1:1 파생(Phase 1b).
 * 컬렉션: pms_docdt_entry.
 */
@Document("pms_docdt_entry")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PmsDocDtEntryDocument {

    /** blKey + "#" + fdId 복합 키. */
    @Id
    private String id;

    private String blKey;

    /** 정렬 키(숫자). */
    private Long blId;

    /** 정렬 tie-break. */
    private String blType;

    /** financial_document.performance_dt. */
    private String perfPd;

    /** financial_document.document_dt. */
    private String docDt;

    /** document_type. */
    private String docType;

    /** document_status. */
    private String status;

    /** group_financial_no IS NOT NULL. */
    private boolean grouped;

    // ── document(fd) 레벨 필터(residual용) ──
    /** financial_document.team_code (PmsBlDocEmbedded.team). */
    private String teamCode;
    /** PmsBlDocEmbedded.operator. */
    private String operator;

    // ── B/L 레벨 필터 필드(비정규화) ──
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
}
