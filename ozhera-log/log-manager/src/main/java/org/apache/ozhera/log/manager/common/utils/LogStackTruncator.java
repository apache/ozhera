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

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Intelligent log stack trace truncator for AI analysis.
 * Supports Java, Go, Python, and JavaScript/TypeScript stack traces.
 * Preserves key error information while reducing token count.
 */
@Slf4j
public class LogStackTruncator {

    private static final Encoding TOKENIZER = Encodings.newDefaultEncodingRegistry()
            .getEncoding(EncodingType.CL100K_BASE);

    private static final int DEFAULT_MAX_TOKENS = 18000;
    private static final int MAX_STACK_FRAMES_PER_ERROR = 15;
    private static final int MAX_CAUSE_DEPTH = 3;

    // Java stack trace patterns
    private static final Pattern JAVA_EXCEPTION_START = Pattern.compile(
            "^(\\w+\\.)*\\w+(Exception|Error|Throwable).*$");
    private static final Pattern JAVA_STACK_FRAME = Pattern.compile(
            "^\\s+at\\s+[\\w.$<>]+\\([^)]+\\)$");
    private static final Pattern JAVA_CAUSED_BY = Pattern.compile(
            "^Caused by:\\s+.*$");
    private static final Pattern JAVA_MORE_FRAMES = Pattern.compile(
            "^\\s+\\.\\.\\.\\s+\\d+\\s+more$");

    // Go stack trace patterns
    private static final Pattern GO_PANIC = Pattern.compile(
            "^panic:\\s+.*$");
    private static final Pattern GO_GOROUTINE = Pattern.compile(
            "^goroutine\\s+\\d+\\s+\\[.*\\]:$");
    private static final Pattern GO_STACK_FRAME = Pattern.compile(
            "^\\s*[\\w./]+\\.\\w+\\(.*\\)$");
    private static final Pattern GO_FILE_LINE = Pattern.compile(
            "^\\s+/.*:\\d+.*$");

    // Python stack trace patterns
    private static final Pattern PYTHON_TRACEBACK = Pattern.compile(
            "^Traceback \\(most recent call last\\):$");
    private static final Pattern PYTHON_FILE_LINE = Pattern.compile(
            "^\\s+File \".*\", line \\d+.*$");
    private static final Pattern PYTHON_EXCEPTION = Pattern.compile(
            "^\\w+Error:\\s+.*$|^\\w+Exception:\\s+.*$");

    // JavaScript/TypeScript stack trace patterns
    private static final Pattern JS_ERROR = Pattern.compile(
            "^(\\w+Error|\\w+Exception):\\s+.*$");
    private static final Pattern JS_STACK_FRAME = Pattern.compile(
            "^\\s+at\\s+.*\\(.*:\\d+:\\d+\\)$|^\\s+at\\s+.*:\\d+:\\d+$");

    public static class TruncationResult {
        private final List<String> truncatedLogs;
        private final int originalTokenCount;
        private final int truncatedTokenCount;
        private final boolean wasTruncated;
        private final String detectedLanguage;

        public TruncationResult(List<String> truncatedLogs, int originalTokenCount,
                                int truncatedTokenCount, boolean wasTruncated, String detectedLanguage) {
            this.truncatedLogs = truncatedLogs;
            this.originalTokenCount = originalTokenCount;
            this.truncatedTokenCount = truncatedTokenCount;
            this.wasTruncated = wasTruncated;
            this.detectedLanguage = detectedLanguage;
        }

        public List<String> getTruncatedLogs() {
            return truncatedLogs;
        }

        public int getOriginalTokenCount() {
            return originalTokenCount;
        }

        public int getTruncatedTokenCount() {
            return truncatedTokenCount;
        }

        public boolean wasTruncated() {
            return wasTruncated;
        }

        public String getDetectedLanguage() {
            return detectedLanguage;
        }
    }

    /**
     * Truncate logs intelligently to fit within token limits while preserving key error information.
     *
     * @param logs      List of log lines
     * @param maxTokens Maximum tokens allowed
     * @return TruncationResult containing truncated logs and metadata
     */
    public static TruncationResult truncate(List<String> logs, int maxTokens) {
        if (logs == null || logs.isEmpty()) {
            return new TruncationResult(logs, 0, 0, false, "unknown");
        }

        String combined = String.join("\n", logs);
        int originalTokenCount = TOKENIZER.countTokens(combined);

        if (originalTokenCount <= maxTokens) {
            return new TruncationResult(logs, originalTokenCount, originalTokenCount, false, detectLanguage(logs));
        }

        String detectedLanguage = detectLanguage(logs);
        List<String> truncatedLogs = truncateByLanguage(logs, maxTokens, detectedLanguage);
        String truncatedCombined = String.join("\n", truncatedLogs);
        int truncatedTokenCount = TOKENIZER.countTokens(truncatedCombined);

        // If still over limit after language-specific truncation, apply generic truncation
        if (truncatedTokenCount > maxTokens) {
            truncatedLogs = genericTruncate(truncatedLogs, maxTokens);
            truncatedCombined = String.join("\n", truncatedLogs);
            truncatedTokenCount = TOKENIZER.countTokens(truncatedCombined);
        }

        log.info("Log truncation: original={} tokens, truncated={} tokens, language={}",
                originalTokenCount, truncatedTokenCount, detectedLanguage);

        return new TruncationResult(truncatedLogs, originalTokenCount, truncatedTokenCount, true, detectedLanguage);
    }

    /**
     * Truncate logs with default max tokens.
     */
    public static TruncationResult truncate(List<String> logs) {
        return truncate(logs, DEFAULT_MAX_TOKENS);
    }

    /**
     * Detect the programming language based on stack trace patterns.
     */
    private static String detectLanguage(List<String> logs) {
        int javaScore = 0;
        int goScore = 0;
        int pythonScore = 0;
        int jsScore = 0;

        for (String line : logs) {
            if (JAVA_EXCEPTION_START.matcher(line).matches() || JAVA_STACK_FRAME.matcher(line).matches()) {
                javaScore += 2;
            }
            if (JAVA_CAUSED_BY.matcher(line).matches()) {
                javaScore += 3;
            }

            if (GO_PANIC.matcher(line).matches() || GO_GOROUTINE.matcher(line).matches()) {
                goScore += 3;
            }
            if (GO_STACK_FRAME.matcher(line).matches() || GO_FILE_LINE.matcher(line).matches()) {
                goScore += 1;
            }

            if (PYTHON_TRACEBACK.matcher(line).matches()) {
                pythonScore += 5;
            }
            if (PYTHON_FILE_LINE.matcher(line).matches() || PYTHON_EXCEPTION.matcher(line).matches()) {
                pythonScore += 2;
            }

            if (JS_ERROR.matcher(line).matches()) {
                jsScore += 3;
            }
            if (JS_STACK_FRAME.matcher(line).matches()) {
                jsScore += 2;
            }
        }

        int maxScore = Math.max(Math.max(javaScore, goScore), Math.max(pythonScore, jsScore));
        if (maxScore == 0) {
            return "unknown";
        }
        if (maxScore == javaScore) {
            return "java";
        }
        if (maxScore == goScore) {
            return "go";
        }
        if (maxScore == pythonScore) {
            return "python";
        }
        return "javascript";
    }

    /**
     * Truncate based on detected language.
     */
    private static List<String> truncateByLanguage(List<String> logs, int maxTokens, String language) {
        return switch (language) {
            case "java" -> truncateJavaStack(logs, maxTokens);
            case "go" -> truncateGoStack(logs, maxTokens);
            case "python" -> truncatePythonStack(logs, maxTokens);
            case "javascript" -> truncateJsStack(logs, maxTokens);
            default -> genericTruncate(logs, maxTokens);
        };
    }

    /**
     * Truncate Java stack traces while preserving exception chain and key frames.
     */
    private static List<String> truncateJavaStack(List<String> logs, int maxTokens) {
        List<String> result = new ArrayList<>();
        List<List<String>> exceptions = new ArrayList<>();
        List<String> currentException = new ArrayList<>();
        boolean inException = false;

        for (String line : logs) {
            boolean isExceptionStart = JAVA_EXCEPTION_START.matcher(line).matches();
            boolean isCausedBy = JAVA_CAUSED_BY.matcher(line).matches();
            boolean isStackFrame = JAVA_STACK_FRAME.matcher(line).matches() || JAVA_MORE_FRAMES.matcher(line).matches();

            if (isExceptionStart || isCausedBy) {
                if (!currentException.isEmpty()) {
                    exceptions.add(new ArrayList<>(currentException));
                    currentException.clear();
                }
                currentException.add(line);
                inException = true;
            } else if (isStackFrame && inException) {
                currentException.add(line);
            } else {
                if (!currentException.isEmpty()) {
                    exceptions.add(new ArrayList<>(currentException));
                    currentException.clear();
                }
                inException = false;
                result.add(line);
            }
        }
        if (!currentException.isEmpty()) {
            exceptions.add(currentException);
        }

        // Truncate each exception's stack frames
        int causesKept = 0;
        for (List<String> exception : exceptions) {
            if (exception.isEmpty()) continue;

            String header = exception.get(0);
            boolean isCause = JAVA_CAUSED_BY.matcher(header).matches();

            if (isCause && causesKept >= MAX_CAUSE_DEPTH) {
                result.add("[... additional causes omitted ...]");
                continue;
            }
            if (isCause) {
                causesKept++;
            }

            result.add(header);
            int frameCount = 0;
            for (int i = 1; i < exception.size() && frameCount < MAX_STACK_FRAMES_PER_ERROR; i++) {
                result.add(exception.get(i));
                frameCount++;
            }
            if (exception.size() - 1 > MAX_STACK_FRAMES_PER_ERROR) {
                result.add("    ... " + (exception.size() - 1 - MAX_STACK_FRAMES_PER_ERROR) + " frames omitted ...");
            }
        }

        return result;
    }

    /**
     * Truncate Go stack traces while preserving panic info and key goroutine frames.
     */
    private static List<String> truncateGoStack(List<String> logs, int maxTokens) {
        List<String> result = new ArrayList<>();
        List<List<String>> goroutines = new ArrayList<>();
        List<String> currentGoroutine = new ArrayList<>();
        boolean inGoroutine = false;
        String panicLine = null;

        for (String line : logs) {
            if (GO_PANIC.matcher(line).matches()) {
                panicLine = line;
                continue;
            }
            if (GO_GOROUTINE.matcher(line).matches()) {
                if (!currentGoroutine.isEmpty()) {
                    goroutines.add(new ArrayList<>(currentGoroutine));
                    currentGoroutine.clear();
                }
                currentGoroutine.add(line);
                inGoroutine = true;
            } else if (inGoroutine && (GO_STACK_FRAME.matcher(line).matches() || GO_FILE_LINE.matcher(line).matches())) {
                currentGoroutine.add(line);
            } else if (inGoroutine && line.trim().isEmpty()) {
                goroutines.add(new ArrayList<>(currentGoroutine));
                currentGoroutine.clear();
                inGoroutine = false;
            } else if (!inGoroutine) {
                result.add(line);
            }
        }
        if (!currentGoroutine.isEmpty()) {
            goroutines.add(currentGoroutine);
        }

        if (panicLine != null) {
            result.add(panicLine);
        }

        // Keep first goroutine (usually the one that panicked) in full, truncate others
        int goroutineCount = 0;
        for (List<String> goroutine : goroutines) {
            if (goroutine.isEmpty()) continue;

            if (goroutineCount == 0) {
                result.addAll(goroutine);
            } else if (goroutineCount < 3) {
                result.add(goroutine.get(0));
                int frameCount = 0;
                for (int i = 1; i < goroutine.size() && frameCount < 6; i++) {
                    result.add(goroutine.get(i));
                    frameCount++;
                }
                if (goroutine.size() - 1 > 6) {
                    result.add("    ... " + (goroutine.size() - 1 - 6) + " frames omitted ...");
                }
            } else if (goroutineCount == 3) {
                result.add("[... " + (goroutines.size() - 3) + " additional goroutines omitted ...]");
            }
            goroutineCount++;
            result.add("");
        }

        return result;
    }

    /**
     * Truncate Python stack traces while preserving traceback structure.
     */
    private static List<String> truncatePythonStack(List<String> logs, int maxTokens) {
        List<String> result = new ArrayList<>();
        List<List<String>> tracebacks = new ArrayList<>();
        List<String> currentTraceback = new ArrayList<>();
        boolean inTraceback = false;

        for (String line : logs) {
            if (PYTHON_TRACEBACK.matcher(line).matches()) {
                if (!currentTraceback.isEmpty()) {
                    tracebacks.add(new ArrayList<>(currentTraceback));
                    currentTraceback.clear();
                }
                currentTraceback.add(line);
                inTraceback = true;
            } else if (PYTHON_EXCEPTION.matcher(line).matches()) {
                currentTraceback.add(line);
                tracebacks.add(new ArrayList<>(currentTraceback));
                currentTraceback.clear();
                inTraceback = false;
            } else if (inTraceback && (PYTHON_FILE_LINE.matcher(line).matches() || line.startsWith("    "))) {
                currentTraceback.add(line);
            } else if (!inTraceback) {
                result.add(line);
            }
        }
        if (!currentTraceback.isEmpty()) {
            tracebacks.add(currentTraceback);
        }

        // Python shows most recent call last, so keep the end of the traceback
        for (List<String> traceback : tracebacks) {
            if (traceback.isEmpty()) continue;

            if (traceback.size() <= MAX_STACK_FRAMES_PER_ERROR * 2 + 2) {
                result.addAll(traceback);
            } else {
                // Keep header
                result.add(traceback.get(0));
                // Keep first few frames
                for (int i = 1; i < 5 && i < traceback.size(); i++) {
                    result.add(traceback.get(i));
                }
                result.add("  ... " + (traceback.size() - MAX_STACK_FRAMES_PER_ERROR * 2 - 2) + " frames omitted ...");
                // Keep last frames (most relevant in Python)
                for (int i = Math.max(5, traceback.size() - MAX_STACK_FRAMES_PER_ERROR * 2); i < traceback.size(); i++) {
                    result.add(traceback.get(i));
                }
            }
        }

        return result;
    }

    /**
     * Truncate JavaScript/TypeScript stack traces.
     */
    private static List<String> truncateJsStack(List<String> logs, int maxTokens) {
        List<String> result = new ArrayList<>();
        List<List<String>> errors = new ArrayList<>();
        List<String> currentError = new ArrayList<>();
        boolean inError = false;

        for (String line : logs) {
            if (JS_ERROR.matcher(line).matches()) {
                if (!currentError.isEmpty()) {
                    errors.add(new ArrayList<>(currentError));
                    currentError.clear();
                }
                currentError.add(line);
                inError = true;
            } else if (JS_STACK_FRAME.matcher(line).matches() && inError) {
                currentError.add(line);
            } else {
                if (!currentError.isEmpty()) {
                    errors.add(new ArrayList<>(currentError));
                    currentError.clear();
                }
                inError = false;
                result.add(line);
            }
        }
        if (!currentError.isEmpty()) {
            errors.add(currentError);
        }

        for (List<String> error : errors) {
            if (error.isEmpty()) continue;

            result.add(error.get(0));
            int frameCount = 0;
            for (int i = 1; i < error.size() && frameCount < MAX_STACK_FRAMES_PER_ERROR; i++) {
                result.add(error.get(i));
                frameCount++;
            }
            if (error.size() - 1 > MAX_STACK_FRAMES_PER_ERROR) {
                result.add("    ... " + (error.size() - 1 - MAX_STACK_FRAMES_PER_ERROR) + " frames omitted ...");
            }
        }

        return result;
    }

    /**
     * Generic truncation for unknown log formats.
     * Keeps first and last portions of the log.
     */
    private static List<String> genericTruncate(List<String> logs, int maxTokens) {
        List<String> result = new ArrayList<>();

        // Calculate how many lines we can keep
        int totalTokens = TOKENIZER.countTokens(String.join("\n", logs));
        double ratio = (double) maxTokens / totalTokens;

        int linesToKeep = Math.max(10, (int) (logs.size() * ratio * 0.9));
        int headLines = linesToKeep * 2 / 3;
        int tailLines = linesToKeep - headLines;

        if (logs.size() <= linesToKeep) {
            return logs;
        }

        // Keep head
        for (int i = 0; i < headLines && i < logs.size(); i++) {
            result.add(logs.get(i));
        }

        result.add("");
        result.add("[... " + (logs.size() - linesToKeep) + " lines omitted ...]");
        result.add("");

        // Keep tail
        for (int i = Math.max(headLines, logs.size() - tailLines); i < logs.size(); i++) {
            result.add(logs.get(i));
        }

        return result;
    }
}
