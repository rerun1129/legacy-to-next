package com.freightos.bms.application.financialdocument.port.out;

import com.freightos.bms.domain.financialdocument.enums.DocumentType;

/**
 * 서류 번호 채번 아웃바운드 포트.
 * document_no_seq 테이블에 대한 원자적 UPSERT를 추상화한다.
 */
public interface DocumentNumberGenerator {

    /**
     * 해당 종류·월의 다음 시퀀스 번호를 채번한다.
     * INSERT ... ON CONFLICT DO UPDATE ... RETURNING last_seq 방식으로 원자적 증가.
     *
     * @param type 서류 종류
     * @param yymm 발급 연월 (예: "2606")
     * @return 증가 후 last_seq 값
     */
    int nextSeq(DocumentType type, String yymm);
}
