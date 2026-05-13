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
@Table(value = "hot_article", camelToUnderline = false)
public class HotArticle implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Auto)
    private Long id;

    private Long sourceId;

    private String originalTitle;

    private String originalUrl;

    private String originalDesc;

    private String aiTitle;

    private String aiSummary;

    private Integer aiScore;

    private LocalDateTime crawlTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(isLogicDelete = true)
    private Integer isDelete;
}
