package spiritray.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.NNMap;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.order.mapper.OrderCountMapper;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * ClassName:OrderCount
 * Package:spiritray.order.controller
 * Description:
 *
 * @Date:2022/6/14 17:20
 * @Author:灵@email
 */
@RestController
@RequestMapping("/order/count")
public class OrderCountController {
    @Autowired
    private OrderCountMapper orderCountMapper;

    /*统计指定店铺的当前月售卖量*/
    @GetMapping("/month/{storeId}")
    public RpsMsg getMothStoreAllSellCount(@PathVariable String storeId) {
        return new RpsMsg().setData(orderCountMapper.selectAllCommoditySellCountByStoreId(storeId)).setStausCode(200).setMsg("查询成功");
    }

    /*统计指定买家当前未付款、待发货、待收货、未评价的订单数目*/
    @GetMapping("/consumer")
    public RpsMsg getConsumerOrderNum(HttpSession session) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(orderCountMapper.selectConsumerOrderNum((Long) session.getAttribute("phone")));
    }
}
