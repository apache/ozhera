package org.apache.ozhera.log.api.service;

import org.apache.ozhera.log.api.model.agent.LogAgentBaseDTO;
import org.apache.ozhera.log.api.model.agent.SpaceInfo;
import org.apache.ozhera.log.api.model.agent.StoreInfo;
import org.apache.ozhera.log.api.model.agent.TailInfo;
import org.apache.ozhera.log.api.model.agent.UserInfo;

public interface LogAgentApiService {

    //---space---

    String createSpace(SpaceInfo info);

    String updateSpace(SpaceInfo info);

    String deleteSpace(SpaceInfo info);

    String getSpaceById(Long spaceId);

    //---store---

    String getStoresInSpace(Long spaceId);

    String getStoreInfoById(StoreInfo info);

    String createStore(StoreInfo info);

    String updateStore(StoreInfo info);

    String deleteStore(StoreInfo info);

    //---tail---

    String createTail(TailInfo info);

    String updateTail(TailInfo info);

    String deleteTail(TailInfo info);

    String getTailById(Long tailId);

}
