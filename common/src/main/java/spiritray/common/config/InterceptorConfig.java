package spiritray.common.config;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import spiritray.common.interceptor.*;
import spiritray.common.pojo.BO.CommonInf;
import spiritray.common.pojo.BO.ExcludeUriAndMethod;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.SSMap;

import java.util.*;

/**
 * ClassName:InterceptorConfig
 * Package:spriritray.consumer.config
 * Description:
 * 拦截器配置类
 *
 * @Date:2022/4/14 14:41
 * @Author:灵@email
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HttpHeaders getHeader() {
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setConnection("Keep-Alive");
        headers.setCacheControl("no-cache");
        return headers;
    }

    //订单令牌失效集合
    @Bean("orderTokens")
    public Map<String, Long> getOrderTokenMap() {
        return new HashMap<>();
    }

    //退款失败任务集合https://segmentfault.com/a/1190000041364081,注意这个需要保证线程安全，因为读写共享,我们使用Java自提供的线程安全的数组
    @Bean("backFail")
    public List<SSMap> getBackTask() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConfigurationProperties("file.upload.interface")
    public FileUploadInterface getFileUploadInterface() {
        return new FileUploadInterface();
    }

    @Bean("commonInf")
    @ConfigurationProperties("common-inf")
    public CommonInf getCommonInf() {
        return new CommonInf();
    }

    @Bean("storeEUAM")
    @ConfigurationProperties("exinf.exclude")
    public ExcludeUriAndMethod getExcludeUriAndMethod() {
        return new ExcludeUriAndMethod();
    }

    @Bean("jwtInterceptor")
    public JWTInterceptor getJwtInterceptor() {
        return new JWTInterceptor();
    }

    @Bean("storeInterceptor")
    public StoreInterceptor getStoreInterceptor() {
        return new StoreInterceptor();
    }

    @Bean("platAccountInterceptor")
    public PlantAccountInterceptor getPlatAccountInterceptor() {
        return new PlantAccountInterceptor();
    }

    @Bean("platInterceptor")
    public PlatInterceptor getPlatInterceptor() {
        return new PlatInterceptor();
    }

    @Bean("sessionInterceptor")
    public SessionKeepInterceptor getSessionKeepInterceptor() {
        return new SessionKeepInterceptor();
    }


    /*注意要静态资源放行，否则无法访问静态资源*/
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加session保持拦截器
        registry.addInterceptor(getSessionKeepInterceptor())
                .addPathPatterns("/**");
        //添加JWT拦截器
        registry.addInterceptor(getJwtInterceptor())
                .addPathPatterns("/consumer/**")
                .excludePathPatterns("/consumer/register")
                .excludePathPatterns("/consumer/code")
                .excludePathPatterns("/consumer/login/**")
                .excludePathPatterns("/consumer/emailCode/**")
                .addPathPatterns("/seller/enter")
                .addPathPatterns("/store/**")
                //.addPathPatterns("/commodity/**")
                .excludePathPatterns("/commodity/consumer/**")
                .excludePathPatterns("/store/storeInf/status/**")
                .excludePathPatterns("/store/storeInf/license/**")
                .excludePathPatterns("/store/storeLicenseSimple/**")
                .excludePathPatterns("/commodity/plat/check/**")
                .excludePathPatterns("/consumer/cart/**");

        //添加store拦截器
        registry.addInterceptor(getStoreInterceptor())
                .addPathPatterns("/store/**")
                .excludePathPatterns("/store/storeInf/status/**")
                .excludePathPatterns("/store/storeInf/license/**")
                .excludePathPatterns("/store/storeInf/phone")
                .excludePathPatterns("/store/storeLicenseSimple/**")
                .addPathPatterns("/commodity/**")
                .excludePathPatterns("/commodity/commodityName/**")
                .excludePathPatterns("/commodity/order/**")
                .excludePathPatterns("/commodity/consumer/**")
                .excludePathPatterns("/commodity/plat/check/**")
                .excludePathPatterns("/consumer/cart/**");
        //添加plat账户查询拦截器
        registry.addInterceptor(getPlatAccountInterceptor())
                .addPathPatterns("/plant/account/**")
                .excludePathPatterns("/plant/account/category/**")
                .excludePathPatterns("/plant/account/cate/**");

        //添加plat拦截器
        registry.addInterceptor(getPlatInterceptor())
                .addPathPatterns("/commodity/plat/check/**");

    }
}
