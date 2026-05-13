package ai.shizhongying.template.config;

import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地存储静态资源映射
 * 仅当 storage.mode=local 时生效，将本地目录暴露为 HTTP 可访问的静态资源
 *
 * @author super.winner
 */
@Configuration
@ConditionalOnProperty(prefix = "storage", name = "mode", havingValue = "local")
public class StorageWebMvcConfig implements WebMvcConfigurer {

    @Resource
    private StorageConfig storageConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        StorageConfig.Local local = storageConfig.getLocal();
        Path absolute = Paths.get(local.getDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(absolute);
        } catch (Exception ignored) {
            // 运行时在首次写入文件前也会再创建一次，这里失败不阻塞启动
        }
        String location = absolute.toUri().toString();
        registry.addResourceHandler(local.getResourcePattern())
                .addResourceLocations(location);
    }
}
