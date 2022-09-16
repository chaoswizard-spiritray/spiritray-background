package spiritray.seller.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import spiritray.common.pojo.PO.Click;
import spiritray.seller.mapper.ClickMapper;
import spiritray.seller.mapper.CommodityMapper;

import java.util.List;
import java.util.UUID;

/**
 * ClassName:CommoditySchedule
 * Package:spiritray.seller.schedule
 * Description:
 *
 * @Date:2022/6/19 21:21
 * @Author:灵@email
 */
@Component
public class CommoditySchedule {
    @Autowired
    private ClickMapper clickMapper;

    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /*每天晚上12点将点击数据插入数据库*/
    @Scheduled(cron = "0 0 12 ? * *")
    private void updateCommodityClickNum() {
        List<String> strings = commodityMapper.selectAllCommodityId();
        for (String string : strings) {
            long num = redisTemplate.opsForSet().size(string);
            //写入数据库
            if (num > 0) {
                clickMapper.insertClick(new Click().setClickNo(String.valueOf(UUID.randomUUID())).setClickNum(num).setCommodityId(string));
            }
            //删除变量
            redisTemplate.opsForValue().getAndDelete(string);
        }
    }
}
