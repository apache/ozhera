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

package org.apache.ozhera.trace.etl.domain.metadata;


import lombok.Data;

import java.io.Serializable;

@Data
public class HeraMetaDataQuery implements Serializable {

    private Long id;

    private Integer page;
    
    private Integer pageSize;

    private Integer offset;
    
    private Integer limit;

    public void initPageParam(){

        if(getPage() == null || getPage() <=0){
            setPage(1);
        }
        if(getPageSize() == null || getPageSize().intValue() <=0){
            setPageSize(10);
        }
        
        setOffset((page -1) * pageSize);
        setLimit(pageSize);
    }


}