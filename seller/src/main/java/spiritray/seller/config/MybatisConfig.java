package spiritray.seller.config;

import lombok.SneakyThrows;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * ClassName:MybatisConfig
 * Package:spiritray.consumer.config
 * Description:
 *
 * @Date:2022/4/14 19:58
 * @Author:灵@email
 */
@Configuration
public class MybatisConfig {
    @Bean
    @ConfigurationProperties("mybatis.configuration")
    public org.apache.ibatis.session.Configuration configuration() {
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        return configuration;
    }

    @SneakyThrows
    @Bean
    @Primary
    @ConfigurationProperties("mybatis")//这个注解就是将mybatis前缀的值映射到这个对象中，等价与配置了Configuration，并进行字段映射后再手动注入。
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource, org.apache.ibatis.session.Configuration configuration) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setVfs(SpringBootVFS.class);
        sqlSessionFactoryBean.setConfiguration(configuration);
        SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
        return sqlSessionFactory;
    }
}
