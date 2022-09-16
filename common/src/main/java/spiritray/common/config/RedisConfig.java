package spiritray.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.Resource;

/**
 * ClassName:RedisConfig
 * Package:spiritray.common.config
 * Description:
 *
 * @Date:2022/5/26 22:32
 * @Author:灵@email
 */
@Configuration
public class RedisConfig {

    //自定义模板,注册到spring
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        //先new一个官方提供的模板
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        //指定连接工厂
        template.setConnectionFactory(factory);
        //json序列化配置
        Jackson2JsonRedisSerializer<Object> objectJackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<Object>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        objectJackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        //String的序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //key采用String的序列化方式
        template.setKeySerializer(stringRedisSerializer);
        //hash采用String的序列化方式
        template.setHashKeySerializer(stringRedisSerializer);
        //value采用jackson
        template.setValueSerializer(objectJackson2JsonRedisSerializer);
        //hash的value序列化采用jackson
        template.setHashValueSerializer(objectJackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        //启动事务redis事务,因为由源码知道默认是false，我们需要设置为true
        template.setEnableTransactionSupport(true);
        return template;

    }

}

