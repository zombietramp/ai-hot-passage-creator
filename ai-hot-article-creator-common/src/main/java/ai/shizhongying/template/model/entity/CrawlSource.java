package ai.shizhongying.template.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(value = "crawl_source", camelToUnderline = false)
public class CrawlSource implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
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

    @Column(isLogicDelete = true)
    private Integer isDelete;
}
