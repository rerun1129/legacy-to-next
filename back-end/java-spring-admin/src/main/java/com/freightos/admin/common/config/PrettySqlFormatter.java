package com.freightos.admin.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PrettySqlFormatter implements MessageFormattingStrategy {
    private static final long DEDUP_WINDOW_NANOS = 100_000_000L;
    private static final ThreadLocal<LastEmission> LAST = new ThreadLocal<>();

    private static final int COLS_PER_LINE = 5;
    private static final String INDENT = "    ";
    private static final String SUB_INDENT = "  ";

    private static final Set<String> CLAUSE_1 = Set.of(
            "select", "from", "where", "having", "offset", "fetch",
            "union", "values", "set", "returning", "limit"
    );
    private static final Set<String> CLAUSE_2 = Set.of(
            "group by", "order by", "union all", "insert into", "delete from"
    );
    private static final Set<String> JOIN_2 = Set.of(
            "inner join", "left join", "right join", "cross join", "full join"
    );
    private static final Set<String> JOIN_3 = Set.of(
            "left outer join", "right outer join", "full outer join"
    );

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
        return System.lineSeparator() + prettyFormat(sql) + System.lineSeparator()
             + "  [" + elapsed + " ms]"
             + (caller.isEmpty() ? "" : System.lineSeparator() + "  called from: " + caller);
    }

    private static String prettyFormat(String sql) {
        String normalized = sql.replaceAll("\\s+", " ").trim();
        List<String> tokens = tokenize(normalized);
        StringBuilder out = new StringBuilder();
        int parenDepth = 0;
        boolean inFilter = false;

        int i = 0;
        while (i < tokens.size()) {
            String tok = tokens.get(i);
            String lower = tok.toLowerCase(Locale.ROOT);
            String two = (i + 1 < tokens.size())
                    ? lower + " " + tokens.get(i + 1).toLowerCase(Locale.ROOT) : null;
            String three = (i + 2 < tokens.size())
                    ? two + " " + tokens.get(i + 2).toLowerCase(Locale.ROOT) : null;

            if (parenDepth == 0) {
                if (three != null && JOIN_3.contains(three)) {
                    newline(out); out.append(three); i += 3; inFilter = false; continue;
                }
                if (two != null && CLAUSE_2.contains(two)) {
                    newline(out); out.append(two); i += 2; inFilter = false; continue;
                }
                if (two != null && JOIN_2.contains(two)) {
                    newline(out); out.append(two); i += 2; inFilter = false; continue;
                }
                if (CLAUSE_1.contains(lower)) {
                    newline(out);
                    if (lower.equals("select")) {
                        out.append("select");
                        i = emitSelectColumns(tokens, i + 1, out);
                        inFilter = false;
                        continue;
                    }
                    out.append(lower);
                    i++;
                    inFilter = lower.equals("where") || lower.equals("having");
                    continue;
                }
                if (lower.equals("join")) {
                    newline(out); out.append("join"); i++; inFilter = false; continue;
                }
                if (lower.equals("on")) {
                    newline(out); out.append("on"); i++; inFilter = true; continue;
                }
                if (inFilter && (lower.equals("and") || lower.equals("or"))) {
                    newline(out); out.append(SUB_INDENT).append(lower); i++; continue;
                }
            }

            appendToken(out, tok);
            if (tok.equals("(")) parenDepth++;
            else if (tok.equals(")")) parenDepth--;
            i++;
        }

        return out.toString();
    }

    private static int emitSelectColumns(List<String> tokens, int start, StringBuilder out) {
        int parenDepth = 0;
        List<List<String>> columns = new ArrayList<>();
        List<String> current = new ArrayList<>();

        int i = start;
        while (i < tokens.size()) {
            String tok = tokens.get(i);
            String lower = tok.toLowerCase(Locale.ROOT);
            if (parenDepth == 0) {
                String two = (i + 1 < tokens.size())
                        ? lower + " " + tokens.get(i + 1).toLowerCase(Locale.ROOT) : null;
                if (CLAUSE_1.contains(lower)
                        || (two != null && CLAUSE_2.contains(two))) {
                    break;
                }
            }
            if (tok.equals("(")) parenDepth++;
            else if (tok.equals(")")) parenDepth--;
            if (parenDepth == 0 && tok.equals(",")) {
                if (!current.isEmpty()) {
                    columns.add(current);
                    current = new ArrayList<>();
                }
            } else {
                current.add(tok);
            }
            i++;
        }
        if (!current.isEmpty()) columns.add(current);

        for (int j = 0; j < columns.size(); j++) {
            if (j % COLS_PER_LINE == 0) {
                out.append(System.lineSeparator()).append(INDENT);
                if (j > 0) out.append(", ");
            } else {
                out.append(", ");
            }
            out.append(joinTokens(columns.get(j)));
        }
        return i;
    }

    private static List<String> tokenize(String sql) {
        List<String> tokens = new ArrayList<>();
        int n = sql.length();
        int i = 0;
        while (i < n) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            if (c == '\'' || c == '"') {
                char q = c;
                int start = i;
                i++;
                while (i < n) {
                    if (sql.charAt(i) == q) {
                        if (i + 1 < n && sql.charAt(i + 1) == q) { i += 2; continue; }
                        i++;
                        break;
                    }
                    i++;
                }
                tokens.add(sql.substring(start, i));
                continue;
            }
            if (c == ',' || c == '(' || c == ')') {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }
            int start = i;
            while (i < n) {
                char d = sql.charAt(i);
                if (Character.isWhitespace(d) || d == ',' || d == '(' || d == ')' || d == '\'' || d == '"') break;
                i++;
            }
            tokens.add(sql.substring(start, i));
        }
        return tokens;
    }

    private static String joinTokens(List<String> tokens) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (sb.length() > 0) {
                String prev = tokens.get(i - 1);
                boolean noSpace = "(".equals(prev) || "(".equals(t) || ")".equals(t) || ",".equals(t);
                if (!noSpace) sb.append(' ');
            }
            sb.append(t);
        }
        return sb.toString();
    }

    private static void appendToken(StringBuilder out, String tok) {
        if (out.length() > 0) {
            char last = out.charAt(out.length() - 1);
            boolean noSpace = last == '\n' || last == '\r' || last == ' ' || last == '('
                    || tok.equals("(") || tok.equals(")") || tok.equals(",");
            if (!noSpace) out.append(' ');
        }
        out.append(tok);
    }

    private static void newline(StringBuilder out) {
        if (out.length() > 0) {
            char last = out.charAt(out.length() - 1);
            if (last != '\n' && last != '\r') out.append(System.lineSeparator());
        }
    }

    private static String findCaller() {
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            String cls = e.getClassName();
            if (cls.startsWith("com.freightos.admin") && !cls.contains("PrettySqlFormatter")) {
                return cls + "." + e.getMethodName()
                     + "(" + e.getFileName() + ":" + e.getLineNumber() + ")";
            }
        }
        return "";
    }

    private record LastEmission(int connectionId, String sql, long timestamp) {}
}
