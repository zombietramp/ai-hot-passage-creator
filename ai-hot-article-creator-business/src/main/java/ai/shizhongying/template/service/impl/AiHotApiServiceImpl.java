package ai.shizhongying.template.service.impl;

import ai.shizhongying.template.model.entity.HotArticle;
import ai.shizhongying.template.service.AiHotApiService;
import ai.shizhongying.template.service.HotArticleService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AiHotApiServiceImpl implements AiHotApiService {

    private static final String API_BASE = "https://aihot.virxact.com/api/public/items";
    private static final int PAGE_SIZE = 30;
    private static final long AIHOT_SOURCE_ID = -1L;

    @Resource
    private HotArticleService hotArticleService;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public void fetchAndSave() {
        log.info("开始从 AIHOT API 拉取数据");
        int saved = 0;

        try {
            String url = API_BASE + "?mode=selected&take=" + PAGE_SIZE;
            String json = doGet(url);
            if (json == null) return;

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray items = root.getAsJsonArray("items");

            for (JsonElement el : items) {
                JsonObject item = el.getAsJsonObject();
                String originalUrl = getStr(item, "url");
                if (originalUrl == null || originalUrl.isBlank()) continue;

                boolean exists = hotArticleService.count(
                        QueryWrapper.create().eq("originalUrl", originalUrl)
                ) > 0;
                if (exists) continue;

                HotArticle article = HotArticle.builder()
                        .sourceId(AIHOT_SOURCE_ID)
                        .originalTitle(getStr(item, "title_en") != null ? getStr(item, "title_en") : getStr(item, "title"))
                        .originalUrl(originalUrl)
                        .originalDesc(getStr(item, "summary"))
                        .aiTitle(getStr(item, "title"))
                        .aiSummary(truncate(getStr(item, "summary"), 1000))
                        .aiScore(estimateScore(item))
                        .crawlTime(parseTime(getStr(item, "publishedAt")))
                        .build();

                hotArticleService.save(article);
                saved++;
            }
        } catch (Exception e) {
            log.error("AIHOT API 拉取失败: {}", e.getMessage(), e);
        }

        log.info("AIHOT API 拉取完成，新增 {} 条", saved);
    }

    private String doGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .header("Accept", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.error("AIHOT API 请求失败: status={}", response.code());
                return null;
            }
            return response.body().string();
        } catch (Exception e) {
            log.error("AIHOT API 网络错误: {}", e.getMessage());
            return null;
        }
    }

    private int estimateScore(JsonObject item) {
        String category = getStr(item, "category");
        if ("ai-models".equals(category) || "ai-products".equals(category)) return 8;
        if ("ai-research".equals(category)) return 7;
        return 6;
    }

    private LocalDateTime parseTime(String isoTime) {
        if (isoTime == null) return LocalDateTime.now();
        try {
            return ZonedDateTime.parse(isoTime, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private String getStr(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return (el != null && !el.isJsonNull()) ? el.getAsString() : null;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
