package com.xiaomi.mone.log.parse;

import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description original log format
 * @date 2023/9/15 16:28
 */
@Slf4j
@NoArgsConstructor
public class RawLogParser implements LogParser {

    private LogParserData parserData;

    public RawLogParser(LogParserData parserData) {
        this.parserData = parserData;
    }

    @Override
    public Map<String, Object> parse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        Map<String, Object> ret = parseSimple(logData, collectStamp);
        extractTimeStamp(ret, logData, collectStamp);
        wrapMap(ret, parserData, ip, lineNum, fileName, collectStamp);
        checkMessageExist(ret, logData);
        return ret;
    }

    @Override
    public Map<String, Object> parseSimple(String logData, Long collectStamp) {
        Map<String, Object> ret = new HashMap<>();
        ret.put(esKeyMap_MESSAGE, logData);
        if (null != collectStamp) {
            ret.put(esKeyMap_timestamp, collectStamp);
        }
        return ret;
    }

    @Override
    public List<String> parseLogData(String logData) {
        return Lists.newArrayList(logData);
    }
}
