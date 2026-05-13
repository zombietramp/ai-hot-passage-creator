package ai.shizhongying.template;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 主类（项目启动入口）
 *
 * @author super.winner
 */
@SpringBootApplication
@MapperScan("ai.shizhongying.template.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiMainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMainApplication.class, args);
    }

}
