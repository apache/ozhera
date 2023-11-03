package com.xiaomi.mone.log.parse;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/31 16:32
 */
public abstract class AbstractLogParser implements LogParser {

    protected LogParserData parserData;

    public AbstractLogParser(LogParserData parserData) {
        this.parserData = parserData;
    }

    @Override
    public Map<String, Object> parse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        Map<String, Object> parseData = doParse(logData, ip, lineNum, collectStamp, fileName);
        extractTimeStamp(parseData, logData, collectStamp);
        wrapMap(parseData, parserData, ip, lineNum, fileName, collectStamp);
        checkMessageExist(parseData, logData);
        validRet(parseData, logData);
        handleImproperTraceId(parseData);
        return parseData;
    }

    private void handleImproperTraceId(Map<String, Object> parseData) {
        if (parseData.containsKey(TRACE_ID_KEY)) {
            Object traceIdValue = parseData.get(TRACE_ID_KEY);
            if (traceIdValue instanceof String) {
                String traceId = (String) traceIdValue;
                if (traceId.startsWith(TRACE_ID_KEY + "=")) {
                    parseData.put(TRACE_ID_KEY, traceId.substring(TRACE_ID_KEY.length() + 1));
                }
            }
        }
    }

    protected void validTimestamp(Map<String, Object> ret, Long collectStamp) {
        /**
         * If the user configures the parse timestamp field, the time format is checked to be correct, and the incorrect time is set to the current time
         */
        if (ret.containsKey(esKeyMap_timestamp)) {
            Long time = getTimestampFromString(ret.get(esKeyMap_timestamp).toString(), collectStamp);
            ret.put(esKeyMap_timestamp, time);
        }
    }


    /**
     * Time extraction
     */
    void extractTimeStamp(Map<String, Object> ret, String logData, Long collectStamp) {
        /**
         * The first [2022 XXXX] in extracted text, the first default is time processing
         */
        if (!ret.containsKey(esKeyMap_timestamp) && logData.startsWith(LOG_PREFIX)) {
            String timeStamp = StringUtils.substringBetween(logData, LOG_PREFIX, LOG_SUFFFIX);
            Long time = getTimestampFromString(timeStamp, collectStamp);
            ret.put(esKeyMap_timestamp, time);
        }
        /**
         * Special handling, only dates starting with a date in the file such as yyyy-mm-dd HH:mm:ss will be extracted
         */
        if (!ret.containsKey(esKeyMap_timestamp) && logData.startsWith(specialTimePrefix)) {
            String timeStamp = StringUtils.substring(logData, 0, specialTimeLength);
            Long time = getTimestampFromString(timeStamp, collectStamp);
            ret.put(esKeyMap_timestamp, time);
        }
    }

    void wrapMap(Map<String, Object> ret, LogParserData parserData, String ip,
                 Long lineNum, String fileName, Long collectStamp) {
        ret.putIfAbsent(esKeyMap_timestamp, null == collectStamp ? getTimestampFromString("", collectStamp) : collectStamp);
        ret.put(esKeyMap_topic, parserData.getTopicName());
        ret.put(esKeyMap_tag, parserData.getMqTag());
        ret.put(esKeyMap_logstoreName, parserData.getLogStoreName());
        ret.put(esKeyMap_tail, parserData.getTailName());
        ret.put(esKeyMap_logip, ip);
        ret.put(esKeyMap_lineNumber, lineNum);
        ret.put(esKyeMap_fileName, fileName);
    }

    void checkMessageExist(Map<String, Object> ret, String originData) {
        if (!ret.containsKey(esKeyMap_MESSAGE)) {
            ret.put(esKeyMap_MESSAGE, originData);
            ret.remove(esKeyMap_logSource);
        }
    }

    /**
     * Field configuration error check If the value corresponding to the key is empty in the result,
     * indicating that the corresponding key has not been extracted, the complete log is retained
     */
    void validRet(Map<String, Object> ret, String logData) {
        if (ret.values().stream().filter(Objects::nonNull).map(String::valueOf).anyMatch(StringUtils::isEmpty)) {
            ret.put(esKeyMap_logSource, logData);
        }
    }

    public abstract Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName);

}
