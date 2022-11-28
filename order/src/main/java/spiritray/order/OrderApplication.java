package spiritray.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import spiritray.common.config.InterceptorConfig;
import spiritray.common.config.RedisConfig;
import spiritray.common.config.ThreadPoolConfig;
import spiritray.common.config.ZoneCorsConfig;
import spiritray.order.config.RedisListenerConfig;

@SpringBootApplication
@Import({ZoneCorsConfig.class, InterceptorConfig.class, RedisConfig.class, RedisListenerConfig.class, ThreadPoolConfig.class})
@MapperScan(basePackages = "spiritray.order.mapper")
@EnableScheduling
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
