package ai.shizhongying.template.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置
 *
 * @author super.winner
 */
@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageConfig {

    public static final String MODE_LOCAL = "local";
    public static final String MODE_COS = "cos";

    /**
     * 存储模式：local 或 cos，默认 cos
     */
    private String mode = MODE_COS;

    /**
     * 本地存储配置
     */
    private Local local = new Local();

    public boolean isLocal() {
        return MODE_LOCAL.equalsIgnoreCase(mode);
    }

    @Data
    public static class Local {

        /**
         * 本地文件保存根目录（绝对或相对路径）
         */
        private String dir = "./data/uploads";

        /**
         * 对外访问 URL 前缀，例如 http://localhost:8880/ai-hot/api/static
         */
        private String urlPrefix = "http://localhost:8880/ai-hot/api/static";

        /**
         * 对应前缀在 Spring MVC 中的资源路径，例如 /static/**
         */
        private String resourcePattern = "/static/**";
    }
}
