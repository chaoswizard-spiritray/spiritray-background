package spiritray.plant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import spiritray.common.config.*;

@SpringBootApplication
@Import({ZoneCorsConfig.class, RedisConfig.class, InterceptorConfig.class, ThreadPoolConfig.class, WebSocketConfig.class})
@MapperScan(basePackages = "spiritray.plant.mapper")
public class PlantApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlantApplication.class, args);
    }

}
