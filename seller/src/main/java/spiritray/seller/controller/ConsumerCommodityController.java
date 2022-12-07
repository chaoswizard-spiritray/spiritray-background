package spiritray.seller.controller;

import cn.hutool.extra.tokenizer.TokenizerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.seller.service.ConsumerCommodityService;

import javax.servlet.http.HttpSession;

/**
 * ClassName:ConsumerCommodityController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/6/15 9:19
 * @Author:灵@email
 */
@RestController
@RequestMapping("/commodity/consumer")
public class ConsumerCommodityController {
    @Autowired
    private ConsumerCommodityService consumerCommodityService;

    /*查询首页商品信息*/
    @GetMapping("/home/{pageNum}/{recordNum}")
    public RpsMsg getHomeCommidty(@PathVariable int pageNum, @PathVariable int recordNum, HttpSession session) {
        //已经登录、和未登录是两种状况
        Object l = session.getAttribute("phone");
        if (l != null) {
            return consumerCommodityService.queryHomeCommodity(pageNum, recordNum, (Long) l);
        } else {
            return consumerCommodityService.queryHomeCommodity(pageNum, recordNum, -1);
        }
    }

    /*查询客户端商品详情展示信息*/
    @GetMapping("/info/detail/{commodityId}")
    public RpsMsg getComsumerCommodity(@PathVariable String commodityId) {
        return consumerCommodityService.queryConsumerCommodityDetail(commodityId);
    }

    /*查询搜索商品*/
    @GetMapping("/search/{word}")
    public RpsMsg getComsumerCommoditySearch(@PathVariable String word) {
        consumerCommodityService.queryConsumerCommoditySearch(word);
        return null;
    }

}
