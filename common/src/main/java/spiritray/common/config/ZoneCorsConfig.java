package spiritray.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * ClassName:ZoneCorsConfig
 * Package:spriritray.consumer.config
 * Description:
 *
 * @Date:2022/4/12 16:23
 * @Author:灵@email
 */
@Configuration
public class ZoneCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //允许跨域的路由
        registry.addMapping("/**")
                //允许所有跨域请求
                .allowedOriginPatterns("*")
                //允许cookie通行证
                .allowCredentials(true)
                //允许所有请求方式
                .allowedMethods("*")
                //跨域允许时间
                .maxAge(360000)
                .allowedHeaders("*");
    }

}
