package spiritray.seller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.HotWord;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * ClassName:HotWordController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/12/10 20:00
 * @Author:灵@email
 */
@RestController
@RequestMapping("/hotword")
public class HotWordController {
    @Autowired
    private RedisTemplate redisTemplate;

    /*获取当天的搜索热词
     * 当天热词判断条件：
     * 首先当天搜索量突破100，
     * 然后搜索量占突破100的前20才能作为热词
     * */
    @GetMapping("/today")
    public RpsMsg getHotWordToday() {
        //从redis中获取热词
        if (redisTemplate.opsForZSet().zCard("hotWordSet") == 0) {
            return new RpsMsg().setStausCode(200);
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
        return new RpsMsg().setStausCode(200).setData(words);
    }
}
