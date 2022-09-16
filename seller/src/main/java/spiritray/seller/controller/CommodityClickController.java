package spiritray.seller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;

import javax.servlet.http.HttpSession;

/**
 * ClassName:CommodityClickController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/6/19 20:33
 * @Author:灵@email
 */
@RestController
@RequestMapping("/click")
public class CommodityClickController {

    @Autowired
    private RedisTemplate redisTemplate;

    /*记录点击商品点击量*/
    @PostMapping("/num")
    public RpsMsg addClick(String commodityId, HttpSession session) {
        Long phone = (Long) session.getAttribute("phone");
        redisTemplate.opsForSet().add(commodityId, phone);
        return new RpsMsg().setMsg("添加成功").setStausCode(200);
    }

}
