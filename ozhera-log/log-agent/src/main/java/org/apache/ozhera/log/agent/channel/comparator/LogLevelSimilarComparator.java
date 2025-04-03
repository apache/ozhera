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

