package org.apache.ozhera.log.manager.controller;

import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;
import org.apache.ozhera.log.manager.model.dto.LogAiAnalysisDTO;
import org.apache.ozhera.log.manager.model.vo.LogAiAnalysisResponse;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.service.MilogAiAnalysisService;

import javax.annotation.Resource;

@Controller
public class MilogAiController {

    @Resource
    private MilogAiAnalysisService milogAiAnalysisService;

    @RequestMapping(path = "/milog/tail/aiAnalysis", method = "post")
    public Result<LogAiAnalysisResponse> aiAnalysis(@RequestParam(value = "logList") LogAiAnalysisDTO tailLogAiAnalysisDTO){
        return milogAiAnalysisService.tailLogAiAnalysis(tailLogAiAnalysisDTO);
    }
}
