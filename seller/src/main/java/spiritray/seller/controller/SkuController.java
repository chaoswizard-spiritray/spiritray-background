package spiritray.seller.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.seller.service.SkuService;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:SkuController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/6/14 0:04
 * @Author:灵@email
 */
@RestController
@RequestMapping("/sku")
public class SkuController {
    @Autowired
    private SkuService skuService;

    /*查询指定商品的sku*/
    @GetMapping("/all/{commodityId}")
    public RpsMsg getSkuByCommodityId(@PathVariable String commodityId) {
        return skuService.queryCommoditySku(commodityId);
    }

    /*批量查询指定商品的指定sku*/
    @PutMapping("/checkorder")
    public RpsMsg getCheckInfo(String commodities) {
        //转换参数
        List<SSMap> checkParams = JSONObject.parseArray(commodities).toJavaList(SSMap.class);
        return skuService.queryCheckOrderInfo(checkParams);
    }

    /*批量减少商品数量*/
    @PutMapping("/sub")
    public RpsMsg putSkuNum(String commodities, String commodityNums) {
        //转换参数
        List<SSMap> checkParams = JSONObject.parseArray(commodities).toJavaList(SSMap.class);
        List<Integer> nums = JSONObject.parseArray(commodityNums).toJavaList(Integer.class);
        RpsMsg rpsMsg = null;
        try {
            rpsMsg = skuService.updateSkuNum(checkParams, nums);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getCause());
            return rpsMsg.setMsg("商品数量不足").setStausCode(300);
        }
        return rpsMsg;
    }

    /*增加指定商品的库存量*/
    @PutMapping("/add")
    public RpsMsg addSkuNum(String commodityId, String skuValue, int num) {
        return skuService.addSkuNum(commodityId, skuValue, num);
    }
}
