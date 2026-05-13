package ai.shizhongying.template.model.vo;

import ai.shizhongying.template.model.entity.CrawlSource;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CrawlSourceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String url;

    private String itemSelector;

    private String titleSelector;

    private String linkSelector;

    private String descSelector;

    private String cronExpression;

    private Integer enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static CrawlSourceVO objToVo(CrawlSource source) {
        if (source == null) {
            return null;
        }
        CrawlSourceVO vo = new CrawlSourceVO();
        BeanUtils.copyProperties(source, vo);
        return vo;
    }
}
