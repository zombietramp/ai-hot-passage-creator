package ai.shizhongying.template.model.vo;

import ai.shizhongying.template.model.entity.HotArticle;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class HotArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long sourceId;

    private String sourceName;

    private String originalTitle;

    private String originalUrl;

    private String aiTitle;

    private String aiSummary;

    private Integer aiScore;

    private LocalDateTime crawlTime;

    public static HotArticleVO objToVo(HotArticle hotArticle) {
        if (hotArticle == null) {
            return null;
        }
        HotArticleVO vo = new HotArticleVO();
        BeanUtils.copyProperties(hotArticle, vo);
        return vo;
    }
}
