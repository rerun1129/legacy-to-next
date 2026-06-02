package com.freightos.common.config;

import com.p6spy.engine.common.PreparedStatementInformation;
import com.p6spy.engine.common.ResultSetInformation;
import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * p6spy JdbcEventListener that appends a row-count line immediately after
 * PrettySqlFormatter's SQL block, using the same "p6spy" logger so the
 * two lines appear in sequence in the console output.
 */
@Component
public class SqlRowCountLogger extends JdbcEventListener {

    // Use the named "p6spy" logger to match the existing SQL log lines.
    // Lombok @Slf4j would bind to this class name, so we declare it explicitly.
    private static final Logger LOG = LoggerFactory.getLogger("p6spy");

    @Override
    public void onAfterExecuteUpdate(PreparedStatementInformation statementInformation,
                                     long timeElapsedNanos, int rowCount, SQLException e) {
        if (e != null) return;
        LOG.info("  Affected Rows : {}", rowCount);
    }

    @Override
    public void onAfterExecuteUpdate(StatementInformation statementInformation,
                                     long timeElapsedNanos, String sql,
                                     int rowCount, SQLException e) {
        if (e != null) return;
        LOG.info("  Affected Rows : {}", rowCount);
    }

    @Override
    public void onAfterResultSetClose(ResultSetInformation resultSetInformation, SQLException e) {
        if (e != null) return;
        // p6spy의 currRow는 -1로 시작해 next()=true마다 ++ 되므로 (총 행 수 - 1).
        // 빈 결과셋(-1) 보정을 위해 +1 후 음수 클램핑.
        int total = Math.max(0, resultSetInformation.getCurrRow() + 1);
        LOG.info("  Result Total : {}", total);
    }
}
