/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.manager.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper that turns user-supplied log-query input into parameterized SQL for the
 * Doris-backed log query path, so that the input cannot be used for SQL injection.
 *
 * <p>Values are always emitted as JDBC bind parameters ({@code ?}) and never
 * concatenated into the statement text. Identifiers (table/column names) are
 * validated against a strict allow-list. Any input that cannot be safely
 * represented is rejected (fail-closed) rather than passed through.</p>
 *
 * <p>Supported full-text search grammar:</p>
 * <pre>
 *   expr       := condition ( (AND|OR) condition )*
 *   condition  := identifier operator value
 *   operator   := = | != | &lt;&gt; | &gt; | &gt;= | &lt; | &lt;= | LIKE
 *   value      := 'single-quoted' | "double-quoted" | number
 *   identifier := [A-Za-z_][A-Za-z0-9_]*
 * </pre>
 *
 * <p>NOTE: this grammar covers the search expressions the front-end is known to
 * send (e.g. {@code level="ERROR"}). If a deployment relies on richer syntax
 * (parentheses, IN(...), sub-queries, ...) those queries will be rejected and the
 * grammar must be extended deliberately — never by falling back to raw
 * concatenation.</p>
 */
public final class LogQuerySqlBuilder {

    /** Bare column / table identifier. */
    private static final Pattern IDENTIFIER = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    /**
     * Index / table name coming from server-side store configuration. Slightly
     * broader than {@link #IDENTIFIER} to allow the dots and hyphens commonly
     * found in index names, while still forbidding whitespace, quotes, parentheses
     * and statement terminators.
     */
    private static final Pattern INDEX_NAME = Pattern.compile("^[A-Za-z0-9_.\\-]+$");

    /** identifier operator value */
    private static final Pattern CONDITION =
            Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*(=|!=|<>|>=|<=|>|<|(?i:like))\\s*(.+)$");

    private LogQuerySqlBuilder() {
    }

    /**
     * A fragment of SQL together with the ordered bind parameters it references.
     */
    public static final class SqlWithArgs {
        private final String sql;
        private final List<Object> args;

        public SqlWithArgs(String sql, List<Object> args) {
            this.sql = sql;
            this.args = args;
        }

        public String getSql() {
            return sql;
        }

        public List<Object> getArgs() {
            return args;
        }
    }

    /**
     * Validate a table/index name that must be inlined into the statement text
     * (table names cannot be bound as parameters).
     *
     * @throws IllegalArgumentException if the value is not a safe index name
     */
    public static String safeIndexName(String value) {
        if (value == null || !INDEX_NAME.matcher(value).matches()) {
            throw new IllegalArgumentException("Illegal log store index name: " + value);
        }
        return value;
    }

    /**
     * Validate a column identifier (sort key, tail field, ...) that must be
     * inlined into the statement text.
     *
     * @throws IllegalArgumentException if the value is not a safe identifier
     */
    public static String safeIdentifier(String value, String field) {
        if (value == null || !IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Illegal identifier for " + field + ": " + value);
        }
        return value;
    }

    /**
     * Parse the full-text search expression into a parameterized SQL fragment.
     *
     * @return the fragment and its bind parameters, or {@code null} when the
     *         expression is blank
     * @throws IllegalArgumentException if the expression does not match the
     *         supported grammar
     */
    public static SqlWithArgs buildFullTextSearch(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return null;
        }
        List<String> tokens = splitTopLevel(expr.trim());
        StringBuilder sql = new StringBuilder();
        List<Object> args = new ArrayList<>();
        for (int idx = 0; idx < tokens.size(); idx++) {
            String token = tokens.get(idx).trim();
            if (idx % 2 == 1) {
                // connector: already normalized to AND / OR by the scanner
                sql.append(' ').append(token).append(' ');
                continue;
            }
            Matcher matcher = CONDITION.matcher(token);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Illegal search expression: " + token);
            }
            String field = matcher.group(1);
            String operator = normalizeOperator(matcher.group(2));
            Object value = parseValue(matcher.group(3).trim());
            sql.append(field).append(' ').append(operator).append(" ?");
            args.add(value);
        }
        return new SqlWithArgs(sql.toString(), args);
    }

    /**
     * Split an expression into alternating condition / connector tokens at the top
     * level, i.e. ignoring AND / OR that appear inside quoted values. The result
     * is [condition, connector, condition, ...] with connectors at odd indices.
     */
    private static List<String> splitTopLevel(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        char quote = 0;
        int i = 0;
        int n = expr.length();
        while (i < n) {
            char c = expr.charAt(i);
            if (quote != 0) {
                current.append(c);
                if (c == quote) {
                    quote = 0;
                }
                i++;
                continue;
            }
            if (c == '\'' || c == '"') {
                quote = c;
                current.append(c);
                i++;
                continue;
            }
            if (c == ' ' || c == '\t') {
                int j = i;
                while (j < n && (expr.charAt(j) == ' ' || expr.charAt(j) == '\t')) {
                    j++;
                }
                String connector = matchConnector(expr, j);
                int after = j + (connector == null ? 0 : connector.length());
                if (connector != null && after < n
                        && (expr.charAt(after) == ' ' || expr.charAt(after) == '\t')) {
                    tokens.add(current.toString());
                    tokens.add(connector.toUpperCase());
                    current.setLength(0);
                    i = after;
                    continue;
                }
            }
            current.append(c);
            i++;
        }
        tokens.add(current.toString());
        return tokens;
    }

    /**
     * @return "AND" / "OR" if the expression contains that keyword at position
     *         {@code pos} (case-insensitive), otherwise {@code null}
     */
    private static String matchConnector(String expr, int pos) {
        if (expr.regionMatches(true, pos, "AND", 0, 3)) {
            return "AND";
        }
        if (expr.regionMatches(true, pos, "OR", 0, 2)) {
            return "OR";
        }
        return null;
    }

    private static String normalizeOperator(String operator) {
        return operator.equalsIgnoreCase("like") ? "LIKE" : operator;
    }

    /**
     * Convert a raw value token into its bind value. Strings must be quoted
     * (single or double); a matching quote inside the literal may be escaped by
     * doubling it. Unquoted tokens are only accepted when numeric.
     */
    private static Object parseValue(String raw) {
        if (raw.length() >= 2) {
            char first = raw.charAt(0);
            char last = raw.charAt(raw.length() - 1);
            if ((first == '"' || first == '\'') && last == first) {
                String inner = raw.substring(1, raw.length() - 1);
                String doubled = String.valueOf(first) + first;
                // A lone (unescaped) quote inside the literal means the token is malformed.
                if (inner.replace(doubled, "").indexOf(first) >= 0) {
                    throw new IllegalArgumentException("Malformed quoted value: " + raw);
                }
                return inner.replace(doubled, String.valueOf(first));
            }
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            // not a long
        }
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException ignored) {
            // not a number either
        }
        throw new IllegalArgumentException("Search value must be quoted or numeric: " + raw);
    }
}
