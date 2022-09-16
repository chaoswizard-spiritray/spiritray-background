package spiritray.order.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spiritray.order.mapper.OrderMapper;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ClassName:OrderSchedule
 * Package:spiritray.order.schedule
 * Description:
 *
 * @Date:2022/6/17 18:57
 * @Author:灵@email
 */
@Component
public class OrderSchedule {
    @Autowired
    @Qualifier("orderTokens")
    private Map tokenMap;

    @Autowired
    @Qualifier("backFail")
    private List backFail;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    //半个小时执行一次
    @Scheduled(fixedRate = 180000)
    private void checkToken() {
        //循环集合
        Iterator iterator = tokenMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry map = (Map.Entry) iterator.next();
            if (System.currentTimeMillis() >= (long) map.getValue()) {
                redisTemplate.opsForSet().remove("orderTokens", map.getKey());
            } else {
                tokenMap.put(map.getKey(), map.getValue());
            }
        }
    }

    /*退款失败任务重新执行*/
    @Scheduled(cron = "0 0 19 ? * *")
    private void execBackFail() {

    }

    //每天4点执行无订单细节订单清除:秒 分 时 日 月 年
    @Scheduled(cron = "0 0 4 ? * *")
    private void clearNoDetailOrder() {
        orderMapper.updateOrderNoDetail();
    }
}
