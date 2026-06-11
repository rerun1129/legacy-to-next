package com.freightos.pms.adapter.out.commoncode;

import com.freightos.pms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.pms.application.enums.projection.EnumOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * admin 스키마 common_code 테이블 JdbcTemplate 읽기 어댑터.
 * PMS는 fms DB를 공유하므로 admin.common_code 직접 조회 가능.
 */
@Slf4j
@Component
public class CommonCodeJdbcAdapter implements CommonCodeReadPort {

    private static final String SQL =
            "SELECT code, label, label_ko " +
            "FROM admin.common_code " +
            "WHERE group_code = ? AND active = true " +
            "ORDER BY sort_order";

    private final JdbcTemplate jdbcTemplate;

    public CommonCodeJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<List<EnumOption>> findByGroupCode(String groupCode) {
        try {
            List<EnumOption> result = jdbcTemplate.query(
                    SQL,
                    (rs, rowNum) -> new EnumOption(
                            rs.getString("code"),
                            rs.getString("label"),
                            null,
                            rs.getString("label_ko")),
                    groupCode);
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        } catch (Exception e) {
            log.warn("CommonCode DB query failed for group '{}': {}", groupCode, e.getMessage());
            return Optional.empty();
        }
    }
}
