package spiritray.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * ClassName:RedisListenerConfig
 * Package:spiritray.common.config
 * Description:
 *
 * @Date:2022/6/19 18:43
 * @Author:灵@email
 */
@Configuration
public class RedisListenerConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory factory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        return container;
    }
}
