package com.freightos.bms.application.port.out;

/**
 * B/L 파생 정보 — freight_header.bl_id 기준으로 FMS에서 읽어온 조회 전용 값.
 * HOUSE → fms.house_bl, MASTER → fms.master_bl 에서 추출.
 */
public record BlDerived(
        String jobDiv,
        String bound,
        String blNo,
        String etd,
        String eta
) {}
