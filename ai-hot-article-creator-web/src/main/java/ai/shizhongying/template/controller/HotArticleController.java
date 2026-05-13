package ai.shizhongying.template.controller;

import com.mybatisflex.core.paginate.Page;
import ai.shizhongying.template.common.BaseResponse;
import ai.shizhongying.template.common.ResultUtils;
import ai.shizhongying.template.annotation.AuthCheck;
import ai.shizhongying.template.model.dto.hot.HotArticleQueryRequest;
import ai.shizhongying.template.model.vo.CrawlSourceVO;
import ai.shizhongying.template.model.vo.HotArticleVO;
import ai.shizhongying.template.service.CrawlSourceService;
import ai.shizhongying.template.service.CrawlerService;
import ai.shizhongying.template.service.HotArticleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/hot")
@Slf4j
public class HotArticleController {

    @Resource
    private HotArticleService hotArticleService;

    @Resource
    private CrawlSourceService crawlSourceService;

    @Resource
    private CrawlerService crawlerService;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    private static final String AIHOT_API = "https://aihot.virxact.com/api/public";

    @GetMapping("/list")
    @Operation(summary = "分页查询热榜")
    public BaseResponse<Page<HotArticleVO>> listHotArticles(HotArticleQueryRequest request) {
        if (request == null) {
            request = new HotArticleQueryRequest();
        }
        Page<HotArticleVO> page = hotArticleService.listByPage(request);
        return ResultUtils.success(page);
    }

    @GetMapping("/sources")
    @Operation(summary = "获取所有抓取源（用于前端筛选）")
    public BaseResponse<List<CrawlSourceVO>> listSources() {
        List<CrawlSourceVO> list = crawlSourceService.list().stream()
                .map(CrawlSourceVO::objToVo)
                .collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    @PostMapping("/trigger-crawl")
    @Operation(summary = "手动触发抓取（管理员）")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> triggerCrawl() {
        crawlerService.crawlAll();
        return ResultUtils.success(true);
    }

    @GetMapping("/all")
    @Operation(summary = "全部 AI 动态（代理 AIHOT API）")
    public BaseResponse<String> allItems(
            @RequestParam(defaultValue = "30") int take,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "all") String mode) {
        StringBuilder url = new StringBuilder(AIHOT_API + "/items?mode=" + mode + "&take=" + take);
        if (cursor != null && !cursor.isBlank()) url.append("&cursor=").append(cursor);
        if (q != null && !q.isBlank()) url.append("&q=").append(q);
        if (category != null && !category.isBlank()) url.append("&category=").append(category);
        String json = proxyGet(url.toString());
        return ResultUtils.success(json);
    }

    @GetMapping("/daily")
    @Operation(summary = "最新 AI 日报（代理 AIHOT API）")
    public BaseResponse<String> latestDaily() {
        String json = proxyGet(AIHOT_API + "/daily");
        return ResultUtils.success(json);
    }

    @GetMapping("/daily/{date}")
    @Operation(summary = "指定日期 AI 日报")
    public BaseResponse<String> dailyByDate(@PathVariable String date) {
        String json = proxyGet(AIHOT_API + "/daily/" + date);
        return ResultUtils.success(json);
    }

    @GetMapping("/dailies")
    @Operation(summary = "日报列表")
    public BaseResponse<String> dailies(@RequestParam(defaultValue = "10") int take) {
        String json = proxyGet(AIHOT_API + "/dailies?take=" + take);
        return ResultUtils.success(json);
    }

    private String proxyGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Accept", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            log.warn("AIHOT API 代理请求失败: url={}, status={}", url, response.code());
            return null;
        } catch (Exception e) {
            log.error("AIHOT API 代理网络错误: {}", e.getMessage());
            return null;
        }
    }
}
