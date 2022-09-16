package spiritray.plant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import spiritray.common.config.InterceptorConfig;
import spiritray.common.config.RedisConfig;
import spiritray.common.config.ZoneCorsConfig;

@SpringBootApplication
@Import({ZoneCorsConfig.class, RedisConfig.class, InterceptorConfig.class})
@MapperScan(basePackages = "spiritray.plant.mapper")
public class PlantApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlantApplication.class, args);
    }

}
