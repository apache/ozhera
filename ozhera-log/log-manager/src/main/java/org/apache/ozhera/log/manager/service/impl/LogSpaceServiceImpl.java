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
package org.apache.ozhera.log.manager.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.xiaomi.mone.tpc.common.enums.NodeUserRelTypeEnum;
import com.xiaomi.mone.tpc.common.enums.UserTypeEnum;
import com.xiaomi.mone.tpc.common.vo.NodeVo;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.db.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.api.enums.LogStructureEnum;
import org.apache.ozhera.log.api.enums.MachineRegionEnum;
import org.apache.ozhera.log.api.enums.OperateEnum;
import org.apache.ozhera.log.api.enums.ProjectSourceEnum;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.common.exception.MilogManageException;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.dao.MilogSpaceDao;
import org.apache.ozhera.log.manager.domain.Tpc;
import org.apache.ozhera.log.manager.model.MilogSpaceParam;
import org.apache.ozhera.log.manager.model.convert.MilogSpaceConvert;
import org.apache.ozhera.log.manager.model.dto.MapDTO;
import org.apache.ozhera.log.manager.model.dto.MilogSpaceDTO;
import org.apache.ozhera.log.manager.model.page.PageInfo;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogSpaceDO;
import org.apache.ozhera.log.manager.service.BaseService;
import org.apache.ozhera.log.manager.service.LogSpaceService;
import org.apache.ozhera.log.manager.user.MoneUser;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LogSpaceServiceImpl extends BaseService implements LogSpaceService {

    @Resource
    private MilogSpaceDao milogSpaceDao;

    @Resource
    private MilogLogstoreDao milogLogstoreDao;

    @Resource
    private TpcSpaceAuthService spaceAuthService;

    @Resource
    private LogTailServiceImpl logTailService;

    @Resource
    private Tpc tpc;

    private static final Cache<String, List<MapDTO<String, Long>>> SPACE_ALL_CACHE = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();

    /**
     * new
     *
     * @param param
     * @return
     */
    @Override
    public Result<String> newMilogSpace(MilogSpaceParam param) {
        if (Objects.isNull(param) || StringUtils.isBlank(param.getSpaceName())) {
            return Result.failParam("Parameter error");
        }

        String spaceName = param.getSpaceName();
        if (milogSpaceDao.verifyExistByName(spaceName)) {
            return Result.failParam("There is a space name of the same name");
        }

        MilogSpaceDO milogSpaceDO = wrapMilogSpaceDO(param);
        wrapBaseCommon(milogSpaceDO, OperateEnum.ADD_OPERATE);

        MilogSpaceDO dbDO = milogSpaceDao.newMilogSpace(milogSpaceDO);
        if (Objects.isNull(dbDO.getId())) {
            return Result.failParam("Space is not saved successfully, please try again");
        }
        String creator = MoneUserContext.getCurrentUser().getUser();
        List<String> otherAdmins = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(param.getAdmins())) {
            creator = param.getAdmins().get(0);
            if (param.getAdmins().size() > 1) {
                otherAdmins = CollectionUtil.sub(param.getAdmins(), 1, param.getAdmins().size());
            }
        }
        com.xiaomi.youpin.infra.rpc.Result tpcResult = spaceAuthService.saveSpacePerm(dbDO, creator);
        addMemberAsync(dbDO.getId(), otherAdmins);

        SPACE_ALL_CACHE.invalidate(buildCacheKey(param.getTenantId()));

        if (tpcResult == null || tpcResult.getCode() != 0) {
            milogSpaceDao.deleteMilogSpace(dbDO.getId());
            log.error("The new space is not associated with a permission system,space:[{}], tpcResult:[{}]", dbDO, tpcResult);
            return Result.failParam("Space is not associated with a permission system");
        }

        return Result.success();
    }

    private void addMemberAsync(Long spaceId, List<String> otherAdmins) {
        if (CollectionUtil.isNotEmpty(otherAdmins)) {
            List<CompletableFuture<Void>> adminAsyncResult = otherAdmins.stream()
                    .map(admin -> CompletableFuture.runAsync(() ->
                            spaceAuthService.addSpaceMember(spaceId, admin, UserTypeEnum.CAS_TYPE.getCode(), NodeUserRelTypeEnum.MANAGER.getCode())))
                    .toList();
            CompletableFuture.allOf(adminAsyncResult.toArray(new CompletableFuture[0])).join();
        }
    }

    private MilogSpaceDO wrapMilogSpaceDO(MilogSpaceParam param) {
        MilogSpaceDO milogSpaceDO = new MilogSpaceDO();
        milogSpaceDO.setSpaceName(param.getSpaceName());
        milogSpaceDO.setDescription(param.getDescription());
        return milogSpaceDO;
    }

    /**
     * getById
     *
     * @param id
     * @return
     */
    @Override
    public Result<MilogSpaceDTO> getMilogSpaceById(Long id) {
        if (null == id) {
            return new Result<>(CommonError.ParamsError.getCode(), "ID cannot be empty");
        }
        MilogSpaceDO milogSpace = milogSpaceDao.getMilogSpaceById(id);
        if (null != milogSpace) {
            MilogSpaceDTO milogSpaceDTO = new MilogSpaceDTO();
            milogSpaceDTO.setCreator(milogSpace.getCreator());
            milogSpaceDTO.setCtime(milogSpace.getCtime());
            milogSpaceDTO.setDescription(milogSpace.getDescription());
            milogSpaceDTO.setId(milogSpace.getId());
            milogSpaceDTO.setTenantId(milogSpace.getTenantId());
            milogSpaceDTO.setTenantName("todo");
            milogSpaceDTO.setUtime(milogSpace.getUtime());
            milogSpaceDTO.setSpaceName(milogSpace.getSpaceName());
            return new Result<>(CommonError.Success.getCode(), CommonError.Success.getMessage(), milogSpaceDTO);
        }
        return new Result<>(CommonError.NOT_EXISTS_DATA.getCode(), CommonError.NOT_EXISTS_DATA.getMessage());
    }

    /**
     * Paginated queries
     *
     * @param spaceName
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Result<PageInfo<MilogSpaceDTO>> getMilogSpaceByPage(String spaceName, Long tenantId, Integer page, Integer pageSize) {
        com.xiaomi.youpin.infra.rpc.Result<PageDataVo<NodeVo>> userPermSpacePage = spaceAuthService.getUserPermSpace(spaceName, page, pageSize);
        PageInfo<MilogSpaceDTO> spaceDTOPageInfo = MilogSpaceConvert.INSTANCE.fromTpcPage(userPermSpacePage.getData());
        return Result.success(spaceDTOPageInfo);
    }

    @Override
    public Result<List<MapDTO<String, Long>>> getMilogSpaces(Long tenantId) {
        int pageNum = 1;
        int defaultPageSize = 300;
        String defaultSpaceKey = buildCacheKey(tenantId);
        List<MapDTO<String, Long>> cachedResult = SPACE_ALL_CACHE.getIfPresent(defaultSpaceKey);

        // return cached result if available
        if (CollectionUtils.isNotEmpty(cachedResult)) {
            return Result.success(cachedResult);
        }

        List<NodeVo> nodeVos = fetchAllNodeVos(pageNum, defaultPageSize);

        // transform nodeVos to MapDTO list
        List<MapDTO<String, Long>> result = nodeVos.parallelStream()
                .map(nodeVo -> new MapDTO<>(nodeVo.getNodeName(), nodeVo.getOutId()))
                .collect(Collectors.toList());

        // cache the result
        SPACE_ALL_CACHE.put(defaultSpaceKey, result);
        return Result.success(result);
    }

    private static String buildCacheKey(Long tenantId) {
        MoneUser currentUser = MoneUserContext.getCurrentUser();
        return String.format("%s-%s", (tenantId == null) ? "local-key" : tenantId.toString(), currentUser.getUser());
    }

    private List<NodeVo> fetchAllNodeVos(int pageNum, int pageSize) {
        List<NodeVo> nodeVos = new ArrayList<>();

        while (true) {
            com.xiaomi.youpin.infra.rpc.Result<PageDataVo<NodeVo>> response = spaceAuthService.getUserPermSpace("", pageNum, pageSize);

            if (response.getCode() != 0) {
                throw new MilogManageException("query space from tpc error");
            }

            PageDataVo<NodeVo> pageData = response.getData();
            if (pageData == null || CollectionUtils.isEmpty(pageData.getList())) {
                break;
            }

            nodeVos.addAll(pageData.getList());

            if (pageData.getList().size() < pageSize) {
                break;
            }
            pageNum++;
        }

        return nodeVos;
    }

    /**
     * update
     *
     * @param param
     * @return
     */
    @Transactional
    @Override
    public Result<String> updateMilogSpace(MilogSpaceParam param) {
        if (null == param || StringUtils.isBlank(param.getSpaceName())) {
            return new Result<>(CommonError.ParamsError.getCode(), "Parameter error", "");
        }

        if (milogSpaceDao.verifyExistByName(param.getSpaceName(), param.getId())) {
            return new Result<>(CommonError.UnknownError.getCode(), "There is a space name of the same name", "");
        }

        MilogSpaceDO milogSpace = milogSpaceDao.queryById(param.getId());
        if (null == milogSpace) {
            return new Result<>(CommonError.ParamsError.getCode(), "logSpace does not exist", "");
        }

        if (Objects.equals(param.getSpaceName(), milogSpace.getSpaceName()) &&
                Objects.equals(param.getDescription(), milogSpace.getDescription())) {
            return Result.success("the logSpace data has not changed");
        }

        if (!tpc.hasPerm(MoneUserContext.getCurrentUser(), param.getId())) {
            return Result.fail(CommonError.UNAUTHORIZED);
        }
        wrapMilogSpace(milogSpace, param);
        wrapBaseCommon(milogSpace, OperateEnum.UPDATE_OPERATE);

        if (milogSpaceDao.update(milogSpace)) {
            com.xiaomi.youpin.infra.rpc.Result tpcResult = spaceAuthService.updateSpaceTpc(param, MoneUserContext.getCurrentUser().getUser());

            SPACE_ALL_CACHE.invalidate(buildCacheKey(param.getTenantId()));
            if (tpcResult == null || tpcResult.getCode() != 0) {
                log.error("Modify the space permission system not associated with it,space:[{}], tpcResult:[{}]", milogSpace, tpcResult);
                return Result.success("To modify the unassociated permission system of space, contact the server-side performance group");
            }
            return Result.success();
        } else {
            log.warn("[MilogSpaceService.updateMilogSpace] update MilogSpace err,spaceName:{},spaceId:{}", param.getSpaceName(), param.getId());
            return new Result<>(CommonError.UnknownError.getCode(), CommonError.UnknownError.getMessage(), "");
        }
    }

    @Transactional
    @Override
    public Result<String> deleteMilogSpace(Long id) {
        if (null == id) {
            return new Result<>(CommonError.ParamsError.getCode(), "ID cannot be empty", "");
        }
        if (!tpc.hasPerm(MoneUserContext.getCurrentUser(), id)) {
            return Result.fail(CommonError.UNAUTHORIZED);
        }
        MilogSpaceDO milogSpace = milogSpaceDao.getMilogSpaceById(id);
        if (null == milogSpace) {
            return new Result<>(CommonError.ParamsError.getCode(), "logSpace does not exist", "");
        }
        List<MilogLogStoreDO> stores = milogLogstoreDao.getMilogLogstoreBySpaceId(id);
        if (stores != null && stores.size() != 0) {
            return new Result<>(CommonError.ParamsError.getCode(), "There is a store under this space and cannot be deleted", "");
        }
        if (milogSpaceDao.deleteMilogSpace(id)) {
            logTailService.deleteConfigRemote(id, id, MachineRegionEnum.CN_MACHINE.getEn(), LogStructureEnum.SPACE);

            SPACE_ALL_CACHE.invalidate(buildCacheKey(milogSpace.getTenantId()));
            com.xiaomi.youpin.infra.rpc.Result tpcResult = spaceAuthService.deleteSpaceTpc(id, MoneUserContext.getCurrentUser().getUser(), MoneUserContext.getCurrentUser().getUserType());
            if (tpcResult == null || tpcResult.getCode() != 0) {
                log.error("Remove the space without associated permission system,space:[{}], tpcResult:[{}]", milogSpace, tpcResult);
                return Result.failParam("To delete a space system that is not associated with it, contact the server performance group");
            }
            return Result.success();
        } else {
            log.warn("[MilogSpaceService.deleteMilogSpace] delete MilogSpace err,spaceId:{}", id);
            return Result.failParam("Space deletion failed, please try again");
        }
    }

    @Override
    public Result<String> setSpacePermission(Long spaceId, String permDeptIds) {
        if (spaceId == null || StringUtils.isEmpty(permDeptIds)) {
            return Result.fail(CommonError.ParamsError);
        }
        MilogSpaceDO space = milogSpaceDao.getMilogSpaceById(spaceId);
        if (!permDeptIds.contains(space.getCreateDeptId())) {
            return Result.fail(CommonError.ParamsError.getCode(), "The Create Department permission cannot be revoked");
        }
        space.setPermDeptId(permDeptIds);
        boolean update = milogSpaceDao.update(space);
        return update ? Result.success() : Result.fail(CommonError.UnknownError);
    }

    @Override
    public MilogSpaceDO buildMiLogSpace(MilogSpaceParam cmd, String appCreator) {
        MilogSpaceDO ms = new MilogSpaceDO();
        wrapMilogSpace(ms, cmd, ProjectSourceEnum.ONE_SOURCE.getSource());
        wrapBaseCommon(ms, OperateEnum.ADD_OPERATE, appCreator);
        return ms;
    }
}
