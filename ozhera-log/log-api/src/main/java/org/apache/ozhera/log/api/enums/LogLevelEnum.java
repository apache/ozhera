package org.apache.ozhera.log.api.enums;

import java.util.ArrayList;
import java.util.List;

public enum LogLevelEnum {
    TRACE("TRACE", 1),
    DEBUG("DEBUG", 2),
    INFO("INFO", 3),
    WARN("WARN", 4),
    ERROR("ERROR", 5),
    FATAL("FATAL", 6);


    private final String name;
    private final int level;

    LogLevelEnum(String name, int level) {
        this.name = name;
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public static List<String> getLevelListToCollection(ComparisonTypeEnum comparison, LogLevelEnum referenceLevel) {
        List<String>  result = new ArrayList<>();
        for (LogLevelEnum level : LogLevelEnum.values()) {
            switch (comparison) {
                case GREATER_THAN -> {
                    if (level.getLevel() > referenceLevel.getLevel()) result.add(level.getName());
                }
                case GREATER_THAN_OR_EQUAL_TO -> {
                    if (level.getLevel() >= referenceLevel.getLevel()) result.add(level.getName());
                }
                case EQUAL_TO -> {
                    if (level.getLevel() == referenceLevel.getLevel()) result.add(level.getName());
                }
                case LESS_THAN_OR_EQUAL_TO -> {
                    if (level.getLevel() <= referenceLevel.getLevel()) result.add(level.getName());
                }
                case LESS_THAN -> {
                    if (level.getLevel() < referenceLevel.getLevel()) result.add(level.getName());
                }
            }
        }
        return result;
    }

}
