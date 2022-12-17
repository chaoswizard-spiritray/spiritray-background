package spiritray.seller.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import spiritray.common.pojo.PO.Click;
import spiritray.common.pojo.PO.HotWord;
import spiritray.seller.mapper.ClickMapper;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.mapper.ConsumerCommodityMapper;
import spiritray.seller.mapper.HotWordMapper;

import java.io.*;
import java.sql.Timestamp;
import java.util.*;

/**
 * ClassName:CommoditySchedule
 * Package:spiritray.seller.schedule
 * Description:
 * https://blog.csdn.net/qq_37807821/article/details/117463611
 * @Date:2022/6/19 21:21
 * @Author:灵@email
 */
@Component
public class CommoditySchedule {
    @Autowired
    private ClickMapper clickMapper;

    @Autowired
    private HotWordMapper hotWordMapper;

    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    private ConsumerCommodityMapper consumerCommodityMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /*每天晚上12点将点击数据插入数据库*/
    @Scheduled(cron = "0 0 12 ? * *")
    private void updateCommodityClickNum() {
        Set<String> commodityIds = redisTemplate.opsForZSet().range("commodityClicks", 0, -1);
        if (commodityIds.size() == 0) {
            return;
        }
        for (String commodityId : commodityIds) {
            double clickNum = redisTemplate.opsForZSet().score("commodityClicks", commodityId);
            clickMapper.insertClick(new Click(String.valueOf(UUID.randomUUID()), commodityId, (long) clickNum, new Timestamp(new Date().getTime())));
        }
        //清除数据
        redisTemplate.delete("commodityClicks");
    }

    /*每天凌晨2点将新增的商品种类、商品品牌写入自定义分词词典*/
    @Scheduled(cron = "0 0 22 ? * *")
    private void updateDict() {
        //获取商品种类、商品品牌
        List<String> list = consumerCommodityMapper.selectTokenCol();
        File file = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + "/dict/commodity.dict");
        //遍历写入自定义分词
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            list.stream().peek(s -> {
                try {
                    writer.write(s + " 5 n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    writer.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).count();
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*每天晚上12点将当天热词信息写入数据库*/
    @Scheduled(cron = "0 0 12 ? * *")
    private void updateHotWord() {
        //获取前二十的搜索词
        if (redisTemplate.opsForZSet().zCard("hotWordSet") == 0) {
            return;
        }
        Set<String> set = null;
        if (redisTemplate.opsForZSet().zCard("hotWordSet") >= 20) {
            set = redisTemplate.opsForZSet().range("hotWordSet", 0, 20);
        } else {
            set = redisTemplate.opsForZSet().range("hotWordSet", 0, -1);
        }
        //获取分数
        Set<HotWord> words = new HashSet<>();
        set.stream().peek(s -> {
            //获取指定分数
            double score = redisTemplate.opsForZSet().score("hotWordSet", s);
            if (score > 100) {
                words.add(new HotWord(s, (long) score, new Timestamp(new Date().getTime())));
            }
        }).count();
        //写入数据
        if (words.size() == 0) {
            return;
        } else {
            hotWordMapper.insertHotWords(words);
        }
        //清除集合
        redisTemplate.delete("hotWordSet");
    }

//    /*每天晚上12点检测已下架超过七天的商品自动移除*/
//    @Scheduled(cron = "0 0 12 ? * *")
}
