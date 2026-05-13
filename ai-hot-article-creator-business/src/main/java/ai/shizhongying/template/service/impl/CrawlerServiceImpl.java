package ai.shizhongying.template.service.impl;

import ai.shizhongying.template.model.entity.CrawlSource;
import ai.shizhongying.template.model.entity.HotArticle;
import ai.shizhongying.template.service.AiHotApiService;
import ai.shizhongying.template.service.CrawlSourceService;
import ai.shizhongying.template.service.CrawlerService;
import ai.shizhongying.template.service.HotArticleService;
import ai.shizhongying.template.utils.GsonUtils;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CrawlerServiceImpl implements CrawlerService {

    @Resource
    private CrawlSourceService crawlSourceService;

    @Resource
    private HotArticleService hotArticleService;

    @Resource
    private DashScopeChatModel chatModel;

    @Resource
    private AiHotApiService aiHotApiService;

    private static final String AI_PROMPT_TEMPLATE = """
            你是一位 AI 情报分析师。请对以下资讯条目进行分析，生成：
            1. 一个吸引眼球的中文标题（不超过50字）
            2. 一句话点评（不超过100字，说明为什么值得关注）
            3. 推荐分（1-10分，10分最高）

            请严格以如下 JSON 格式返回，不要包含其他内容：
            {"aiTitle": "...", "aiSummary": "...", "aiScore": N}

            资讯标题：%s
            资讯摘要：%s
            """;

    @Scheduled(fixedRate = 30 * 60 * 1000, initialDelay = 30000)
    public void scheduledCrawl() {
        log.info("定时抓取任务开始");
        crawlAll();
    }

    @Override
    public void crawlAll() {
        // 优先从 AIHOT 公开 API 拉取（已含 AI 处理后的数据）
        try {
            aiHotApiService.fetchAndSave();
        } catch (Exception e) {
            log.error("AIHOT API 拉取异常: {}", e.getMessage(), e);
        }

        // 再执行自定义爬虫源
        List<CrawlSource> sources = crawlSourceService.listEnabled();
        if (sources.isEmpty()) {
            log.info("无自定义抓取源");
            return;
        }
        for (CrawlSource source : sources) {
            try {
                crawlSource(source);
            } catch (Exception e) {
                log.error("抓取源 [{}] 失败: {}", source.getName(), e.getMessage(), e);
            }
        }
        log.info("全部抓取源处理完毕");
    }

    private void crawlSource(CrawlSource source) {
        log.info("开始抓取: {} ({})", source.getName(), source.getUrl());

        Document doc;
        try {
            doc = Jsoup.connect(source.getUrl())
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();
        } catch (Exception e) {
            log.error("抓取页面失败: {}", source.getUrl(), e);
            return;
        }

        Elements items = doc.select(source.getItemSelector());
        log.info("源 [{}] 找到 {} 个条目", source.getName(), items.size());

        List<HotArticle> newArticles = new ArrayList<>();
        for (Element item : items) {
            try {
                String title = extractText(item, source.getTitleSelector());
                String link = extractLink(item, source.getLinkSelector(), source.getUrl());
                String desc = source.getDescSelector() != null
                        ? extractText(item, source.getDescSelector()) : "";

                if (title == null || title.isBlank() || link == null || link.isBlank()) {
                    continue;
                }

                boolean exists = hotArticleService.count(
                        QueryWrapper.create().eq("originalUrl", link)
                ) > 0;
                if (exists) {
                    continue;
                }

                HotArticle article = HotArticle.builder()
                        .sourceId(source.getId())
                        .originalTitle(title.length() > 500 ? title.substring(0, 500) : title)
                        .originalUrl(link.length() > 1024 ? link.substring(0, 1024) : link)
                        .originalDesc(desc)
                        .crawlTime(LocalDateTime.now())
                        .build();
                newArticles.add(article);
            } catch (Exception e) {
                log.warn("解析条目失败: {}", e.getMessage());
            }
        }

        if (newArticles.isEmpty()) {
            log.info("源 [{}] 无新条目", source.getName());
            return;
        }

        log.info("源 [{}] 新增 {} 条，开始 AI 处理", source.getName(), newArticles.size());
        for (HotArticle article : newArticles) {
            try {
                enrichWithAi(article);
            } catch (Exception e) {
                log.warn("AI 处理失败, title={}: {}", article.getOriginalTitle(), e.getMessage());
                article.setAiTitle(article.getOriginalTitle());
                article.setAiSummary("AI 处理失败");
                article.setAiScore(5);
            }
            hotArticleService.save(article);
        }
        log.info("源 [{}] 处理完成，入库 {} 条", source.getName(), newArticles.size());
    }

    private void enrichWithAi(HotArticle article) {
        String prompt = String.format(AI_PROMPT_TEMPLATE,
                article.getOriginalTitle(),
                article.getOriginalDesc() != null ? article.getOriginalDesc() : "无");

        String result = callLlm(prompt);

        String json = extractJson(result);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        article.setAiTitle(obj.get("aiTitle").getAsString());
        article.setAiSummary(obj.get("aiSummary").getAsString());
        article.setAiScore(obj.get("aiScore").getAsInt());
    }

    private String callLlm(String prompt) {
        ChatResponse response = chatModel.call(new Prompt(new UserMessage(prompt)));
        return response.getResult().getOutput().getText();
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    private String extractText(Element parent, String selector) {
        Element el = parent.selectFirst(selector);
        return el != null ? el.text().trim() : null;
    }

    private String extractLink(Element parent, String selector, String baseUrl) {
        Element el = parent.selectFirst(selector);
        if (el == null) return null;
        String href = el.hasAttr("href") ? el.attr("abs:href") : el.attr("href");
        if (href != null && !href.startsWith("http")) {
            href = baseUrl.replaceAll("/[^/]*$", "/") + href;
        }
        return href;
    }
}
