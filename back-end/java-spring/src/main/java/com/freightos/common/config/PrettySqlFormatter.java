package com.freightos.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.hibernate.engine.jdbc.internal.Formatter;

public class PrettySqlFormatter implements MessageFormattingStrategy {
    private static final Formatter FORMATTER = new BasicFormatterImpl();
    private static final long DEDUP_WINDOW_NANOS = 100_000_000L;
    private static final ThreadLocal<LastEmission> LAST = new ThreadLocal<>();

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {
        if (sql == null || sql.isBlank()) return "";
        long ts = System.nanoTime();
        LastEmission prev = LAST.get();
        if (prev != null && prev.connectionId == connectionId
                && prev.sql.equals(sql) && (ts - prev.timestamp) < DEDUP_WINDOW_NANOS) {
            return "";
        }
        LAST.set(new LastEmission(connectionId, sql, ts));
        String caller = findCaller();
        return FORMATTER.format(sql) + System.lineSeparator()
             + "  [" + elapsed + " ms]"
             + (caller.isEmpty() ? "" : System.lineSeparator() + "  called from: " + caller);
    }

    private static String findCaller() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            String cls = e.getClassName();
            if (cls.startsWith("com.freightos.fms") && !cls.contains("PrettySqlFormatter")) {
                return cls + "." + e.getMethodName()
                     + "(" + e.getFileName() + ":" + e.getLineNumber() + ")";
            }
        }
        return "";
    }

    private record LastEmission(int connectionId, String sql, long timestamp) {}
}
