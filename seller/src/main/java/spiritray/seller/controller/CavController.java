package spiritray.seller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.seller.service.CavService;
import spiritray.seller.service.SkuService;

/**
 * ClassName:CavController
 * Package:spiritray.seller.controller
 * Description:
 * 商品的属性、种类相关请求
 *
 * @Date:2022/4/26 13:30
 * @Author:灵@email
 */
@RestController
@RequestMapping("/cav")
public class CavController {
    @Autowired
    private CavService cavService;

    /*查询商品的种类*/
    @GetMapping("/category/{categoryId}")
    public RpsMsg getCategory(@PathVariable int categoryId) {
        return cavService.queryCategory(categoryId);
    }

    /*查询商品属性*/
    @GetMapping("/attribute/{categoryId}")
    public RpsMsg getAttribute(@PathVariable int categoryId) {
        return cavService.queryAttribute(categoryId);
    }

    /*查询指定商品的单值属性值*/
    @GetMapping("/cav/simple/{commodityId}")
    public RpsMsg getSimpleCav(@PathVariable String commodityId) {
        return cavService.queryCavByCommodityId(commodityId, false);
    }

    /*查询商品多值属性值*/
    @GetMapping("/cav/mul/{commodityId}")
    public RpsMsg getMulCav(@PathVariable String commodityId) {
        RpsMsg data = cavService.queryCavByCommodityId(commodityId, true);
        return data;
    }
}
