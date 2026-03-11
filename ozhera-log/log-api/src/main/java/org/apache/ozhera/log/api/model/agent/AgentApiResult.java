package org.apache.ozhera.log.api.model.agent;

import com.google.gson.Gson;

import java.util.LinkedHashMap;
import java.util.Map;

public class AgentApiResult {

    private static final Gson GSON = new Gson();
    private static final String SUCCESS_MESSAGE = "success";

    private boolean success;
    private String message;
    private Map<String, Object> data;

    private AgentApiResult(boolean success, String message, Map<String, Object> data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static String success() {
        return GSON.toJson(new AgentApiResult(true, SUCCESS_MESSAGE, null));
    }

    public static String fail(String message) {
        return GSON.toJson(new AgentApiResult(false, message, null));
    }

    public static Builder successBuilder() {
        return new Builder(true, SUCCESS_MESSAGE);
    }

    public static Builder failBuilder(String message) {
        return new Builder(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static class Builder {
        private boolean success;
        private String message;
        private Map<String, Object> data;

        private Builder(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Builder addData(String key, Object value) {
            if (this.data == null) {
                this.data = new LinkedHashMap<>();
            }
            this.data.put(key, value);
            return this;
        }

        public String build() {
            return GSON.toJson(new AgentApiResult(success, message, data));
        }
    }
}
