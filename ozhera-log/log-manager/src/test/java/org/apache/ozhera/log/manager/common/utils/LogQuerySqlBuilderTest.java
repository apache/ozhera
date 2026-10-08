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

import org.apache.ozhera.log.manager.common.utils.LogQuerySqlBuilder.SqlWithArgs;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class LogQuerySqlBuilderTest {

    @Test
    public void blankExpressionYieldsNoFragment() {
        assertNull(LogQuerySqlBuilder.buildFullTextSearch(null));
        assertNull(LogQuerySqlBuilder.buildFullTextSearch("   "));
    }

    @Test
    public void singleCondition() {
        SqlWithArgs result = LogQuerySqlBuilder.buildFullTextSearch("level=\"ERROR\"");
        assertEquals("level = ?", result.getSql());
        assertEquals(Arrays.asList("ERROR"), result.getArgs());
    }

    @Test
    public void multipleConditionsAreParameterized() {
        SqlWithArgs result = LogQuerySqlBuilder.buildFullTextSearch("level=\"ERROR\" AND code=500");
        assertEquals("level = ? AND code = ?", result.getSql());
        assertEquals(Arrays.asList("ERROR", 500L), result.getArgs());
    }

    @Test
    public void likeOperatorIsPreserved() {
        SqlWithArgs result = LogQuerySqlBuilder.buildFullTextSearch("message like \"%timeout%\"");
        assertEquals("message LIKE ?", result.getSql());
        assertEquals(Arrays.asList("%timeout%"), result.getArgs());
    }

    @Test
    public void andInsideQuotedValueIsNotTreatedAsConnector() {
        SqlWithArgs result = LogQuerySqlBuilder.buildFullTextSearch("message=\"a AND b\"");
        assertEquals("message = ?", result.getSql());
        assertEquals(Arrays.asList("a AND b"), result.getArgs());
    }

    @Test
    public void unionInjectionIsRejected() {
        assertRejected("1=0 UNION SELECT pwd FROM secret_users");
    }

    @Test
    public void statementTerminatorInjectionIsRejected() {
        assertRejected("level=\"x\"; DROP TABLE hera_logs");
    }

    @Test
    public void commentInjectionIsRejected() {
        assertRejected("level=\"x\" OR 1=1 -- ");
    }

    @Test
    public void bareUnquotedStringIsRejected() {
        assertRejected("level=ERROR");
    }

    @Test
    public void identifierAllowsPlainColumn() {
        assertEquals("timestamp", LogQuerySqlBuilder.safeIdentifier("timestamp", "sortKey"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void identifierRejectsInjection() {
        LogQuerySqlBuilder.safeIdentifier("timestamp; DROP TABLE t", "sortKey");
    }

    @Test(expected = IllegalArgumentException.class)
    public void indexNameRejectsWhitespace() {
        LogQuerySqlBuilder.safeIndexName("hera_logs UNION SELECT");
    }

    @Test
    public void indexNameAllowsDotsAndHyphens() {
        assertEquals("hera-log.2026", LogQuerySqlBuilder.safeIndexName("hera-log.2026"));
    }

    private static void assertRejected(String expr) {
        try {
            LogQuerySqlBuilder.buildFullTextSearch(expr);
            fail("expected expression to be rejected: " + expr);
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }
}
