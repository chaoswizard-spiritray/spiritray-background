package spiritray.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import spiritray.common.config.ZoneCorsConfig;

@SpringBootApplication
@Import({ZoneCorsConfig.class})
public class FileApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }

}
