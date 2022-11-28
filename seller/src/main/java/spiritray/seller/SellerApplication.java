package spiritray.seller;

import lombok.SneakyThrows;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import spiritray.common.config.InterceptorConfig;
import spiritray.common.config.RedisConfig;
import spiritray.common.config.ThreadPoolConfig;
import spiritray.common.config.ZoneCorsConfig;

@SpringBootApplication
@Import({ZoneCorsConfig.class, RedisConfig.class, InterceptorConfig.class,ThreadPoolConfig.class})
@MapperScan(basePackages = "spiritray.seller.mapper")
@EnableScheduling
public class SellerApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(SellerApplication.class, args);
    }

}
