package spiritray.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import spiritray.common.config.InterceptorConfig;
import spiritray.common.config.RedisConfig;
import spiritray.order.config.RedisListenerConfig;
import spiritray.common.config.ZoneCorsConfig;

@SpringBootApplication
@Import({ZoneCorsConfig.class, InterceptorConfig.class, RedisConfig.class, RedisListenerConfig.class})
@MapperScan(basePackages = "spiritray.order.mapper")
@EnableScheduling
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
