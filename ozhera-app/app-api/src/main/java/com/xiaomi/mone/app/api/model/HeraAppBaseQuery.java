/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.app.api.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2022/3/20 10:32 上午
 */
@Data
@ToString
public class HeraAppBaseQuery implements Serializable {

    private Integer id;

    private String appId;

    private String appName;

    private String appCname;

    private Integer iamTreeId;

    private Integer bindType;

    private Integer platformType;

    private Integer appType;

    private String appLanguage;

    private String envsMapping;

    private Integer status;

    private String participant;

    private String myParticipant;

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

    private String orderByClause;


}
