package com.freightos.fms.application.freight.port.out;

import com.freightos.fms.application.freight.FreightView;
import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.domain.freight.enums.FreightBlType;

import java.util.Optional;

/**
 * 운임 헤더+라인 저장·조회 아웃바운드 포트.
 * Application 계층 정의 — 어댑터 구현체를 직접 참조하지 않음.
 */
public interface FreightInputPort {

    /**
     * 운임 저장(upsert).
     * blType+blId로 헤더 조회 후 없으면 생성, 있으면 헤더 갱신.
     * 라인은 전량 재구성(기존 삭제 후 재삽입).
     */
    void saveFreight(FreightBlType blType, Long blId, FreightInputCommand cmd);

    /** blType+blId로 운임 헤더+라인 조회. 헤더 없으면 Optional.empty(). */
    Optional<FreightView> findFreightByBl(FreightBlType blType, Long blId);

    /** 해당 B/L에 운임 라인이 1건 이상 존재하는지 확인. */
    boolean existsFreightLines(FreightBlType blType, Long blId);

    /** 해당 B/L의 헤더+라인 전체 삭제. */
    void deleteFreight(FreightBlType blType, Long blId);
}
