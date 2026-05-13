package ai.shizhongying.template.model.dto.hot;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class CrawlSourceUpdateRequest implements Serializable {

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
}
