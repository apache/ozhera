package org.apache.ozhera.log.api.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogAgentBaseDTO implements Serializable {

    private StoreInfo storeInfo = new StoreInfo();

    private UserInfo userInfo = new UserInfo();





}
