package ai.shizhongying.template.controller;

import ai.shizhongying.template.annotation.AuthCheck;
import ai.shizhongying.template.common.BaseResponse;
import ai.shizhongying.template.common.DeleteRequest;
import ai.shizhongying.template.common.ResultUtils;
import ai.shizhongying.template.exception.ErrorCode;
import ai.shizhongying.template.exception.ThrowUtils;
import ai.shizhongying.template.model.dto.hot.CrawlSourceAddRequest;
import ai.shizhongying.template.model.dto.hot.CrawlSourceUpdateRequest;
import ai.shizhongying.template.model.entity.CrawlSource;
import ai.shizhongying.template.model.vo.CrawlSourceVO;
import ai.shizhongying.template.service.CrawlSourceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crawl-source")
public class CrawlSourceController {

    @Resource
    private CrawlSourceService crawlSourceService;

    @PostMapping("/add")
    @Operation(summary = "新增抓取源")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addSource(@RequestBody CrawlSourceAddRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getName() == null || request.getName().isBlank(),
                ErrorCode.PARAMS_ERROR, "名称不能为空");
        ThrowUtils.throwIf(request.getUrl() == null || request.getUrl().isBlank(),
                ErrorCode.PARAMS_ERROR, "URL 不能为空");
        ThrowUtils.throwIf(request.getItemSelector() == null || request.getItemSelector().isBlank(),
                ErrorCode.PARAMS_ERROR, "条目选择器不能为空");
        ThrowUtils.throwIf(request.getTitleSelector() == null || request.getTitleSelector().isBlank(),
                ErrorCode.PARAMS_ERROR, "标题选择器不能为空");
        ThrowUtils.throwIf(request.getLinkSelector() == null || request.getLinkSelector().isBlank(),
                ErrorCode.PARAMS_ERROR, "链接选择器不能为空");

        CrawlSource source = new CrawlSource();
        BeanUtils.copyProperties(request, source);
        crawlSourceService.save(source);
        return ResultUtils.success(source.getId());
    }

    @PostMapping("/update")
    @Operation(summary = "更新抓取源")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateSource(@RequestBody CrawlSourceUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        CrawlSource source = crawlSourceService.getById(request.getId());
        ThrowUtils.throwIf(source == null, ErrorCode.NOT_FOUND_ERROR);
        BeanUtils.copyProperties(request, source);
        crawlSourceService.updateById(source);
        return ResultUtils.success(true);
    }

    @PostMapping("/delete")
    @Operation(summary = "删除抓取源")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> deleteSource(@RequestBody DeleteRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.PARAMS_ERROR);
        crawlSourceService.removeById(request.getId());
        return ResultUtils.success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "查询所有抓取源")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<CrawlSourceVO>> listSources() {
        List<CrawlSourceVO> list = crawlSourceService.list().stream()
                .map(CrawlSourceVO::objToVo)
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }
}
