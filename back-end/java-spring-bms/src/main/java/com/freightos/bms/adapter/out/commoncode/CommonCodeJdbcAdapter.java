package com.freightos.bms.adapter.out.commoncode;

import com.freightos.bms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.bms.application.enums.projection.EnumOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * admin 스키마 common_code 테이블 JdbcTemplate 읽기 어댑터.
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
