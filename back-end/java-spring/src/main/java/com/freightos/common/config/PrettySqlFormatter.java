package com.freightos.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;

public class PrettySqlFormatter implements MessageFormattingStrategy {
    private static final Formatter FORMATTER = new BasicFormatterImpl();

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql == null || sql.isBlank()) return "";
        return FORMATTER.format(sql) + System.lineSeparator()
             + "  [" + elapsed + " ms]";
    }
}
