package org.apache.ozhera.intelligence.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TraceAnalysisService {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public String analyzeTraceRoot(String traceId, String env) {
        return null;
    }

}