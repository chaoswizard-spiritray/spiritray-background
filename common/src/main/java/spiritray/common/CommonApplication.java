package spiritray.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import spiritray.common.config.WebSocketConfig;
import spiritray.common.pojo.PO.Seller;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@Import({WebSocketConfig.class})
public class CommonApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommonApplication.class, args);
    }


}
