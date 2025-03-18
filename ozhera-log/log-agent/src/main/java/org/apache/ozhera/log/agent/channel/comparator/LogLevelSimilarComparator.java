package org.apache.ozhera.log.agent.channel.comparator;

import java.util.List;

public class LogLevelSimilarComparator implements SimilarComparator<List<String>>{

    private List<String> oldLogLevels;

    public LogLevelSimilarComparator(List<String> oldLogLevels) {
        this.oldLogLevels = oldLogLevels;
    }
    @Override
    public boolean compare(List<String> newLogLevels) {
        if (oldLogLevels == null && newLogLevels == null) {
            return true;
        }
        if (oldLogLevels!=null && newLogLevels!=null) {
            return isSimilarList(oldLogLevels, newLogLevels);
        }
        return false;
    }


    private boolean isSimilarList(List<String> oldLogLevels, List<String> newLogLevels) {
        if(oldLogLevels == newLogLevels){
            return true;
        }
        oldLogLevels = oldLogLevels.stream().map(String::toLowerCase).distinct().sorted().toList();
        newLogLevels = newLogLevels.stream().map(String::toLowerCase).distinct().sorted().toList();
        return oldLogLevels.equals(newLogLevels);
    }
}

