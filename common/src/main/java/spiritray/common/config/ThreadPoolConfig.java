package spiritray.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spiritray.common.pojo.DTO.SSMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName:ThreaPoolConfig
 * Package:spiritray.common.config
 * Description:
 *  这个配置类配置一些公共的任务集合以及线程池，这些集合我们需要使用Java的线程安全集合
 *
 * @Date:2022/11/24 21:51
 * @Author:灵@email
 */
@Configuration
public class ThreadPoolConfig {
    //订单令牌失效集合,用于清除进入了订单显示界面，申请了主订单号，但是并没有提交订单的时候，主订单号在redis令牌集合中仍占据内存，需要清除。
    @Bean("orderTokens")
    public Map<String, Long> getOrderTokenMap() {
        return new ConcurrentHashMap<>();
    }

    //退款失败任务集合https://segmentfault.com/a/1190000041364081,注意这个需要保证线程安全，因为读写共享,我们使用Java自提供的线程安全的数组
    @Bean("backFail")
    public List<SSMap> getBackTask() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    //转账失败的任务集合
    @Bean("transferFail")
    public List<SSMap> getTransferTask() {
        return Collections.synchronizedList(new ArrayList<>());
    }

}
