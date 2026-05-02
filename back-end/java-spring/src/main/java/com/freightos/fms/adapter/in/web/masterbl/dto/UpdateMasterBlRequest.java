package com.freightos.fms.adapter.in.web.masterbl.dto;

/**
 * PUT /api/master-bl/{id} 요청 본문.
 * 현재 스펙에서는 payload 없이 id만으로 식별하는 최소 구현.
 * 추후 업데이트 필드가 추가되면 이 record를 확장한다.
 */
public record UpdateMasterBlRequest() {
}
