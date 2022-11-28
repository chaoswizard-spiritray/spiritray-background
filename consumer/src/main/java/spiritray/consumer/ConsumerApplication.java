package spiritray.consumer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import spiritray.common.config.InterceptorConfig;
import spiritray.common.config.RedisConfig;
import spiritray.common.config.ThreadPoolConfig;
import spiritray.common.config.ZoneCorsConfig;


@SpringBootApplication
@Import({ZoneCorsConfig.class, RedisConfig.class, InterceptorConfig.class, ThreadPoolConfig.class})
@MapperScan(basePackages = "spiritray.consumer.mapper")
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}
