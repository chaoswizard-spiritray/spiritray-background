package spiritray.order.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.OrderBeforeCommodity;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SNMap;
import spiritray.common.pojo.PO.Address;
import spiritray.order.service.OrderService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:OrderController
 * Package:spiritray.order.controller
 * Description:
 *
 * @Date:2022/6/17 14:28
 * @Author:灵@email
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisTemplate redisTemplate;

    /*查询指定商品数组的过期时间*/
    @GetMapping("/overtime")
    public RpsMsg getOverTime(String orderDetailIds) {
        List<String> ids = JSON.parseArray(orderDetailIds).toJavaList(String.class);
        List<SNMap> data = new ArrayList<>();
        for (String id : ids) {
            SNMap snMap = new SNMap(id, redisTemplate.getExpire(id));
            data.add(snMap);
        }
        return new RpsMsg().setData(data);
    }

    /*获取订单令牌*/
    @GetMapping("/token")
    public RpsMsg getOrderBeforeToken() {
        return orderService.generateOrderToken();
    }

    /*提交订单信息*/
    @PostMapping("/generate")
    public RpsMsg postOrderMul(String orderCommoditys, String address, String orderId, int payCate, HttpSession session, HttpServletRequest request) {
        //内容解析
        List<OrderBeforeCommodity> commodities = JSONArray.parseArray(orderCommoditys).toJavaList(OrderBeforeCommodity.class);
        Address consumerAddress = JSONObject.parseObject(address, Address.class);
        //调用业务逻辑
        return orderService.generateOrderAndDetail(commodities, consumerAddress, orderId, payCate, (Long) session.getAttribute("phone"), request.getHeader("jwt"));
    }

    /*获取订单状态*/
    @GetMapping("/state/{orderId}")
    public RpsMsg getOrderState(@PathVariable String orderId) {
        return orderService.queryStateByOrderNumber(orderId);
    }

    /*查询待付款订单*/
    @GetMapping("/nopay")
    public RpsMsg getNoPay(HttpSession session) {
        return orderService.getOrder((Long) session.getAttribute("phone"), 0);
    }

    /*查询待发货订单*/
    @GetMapping("/notrans")
    public RpsMsg getNoTrans(HttpSession session) {
        return orderService.getOrder((Long) session.getAttribute("phone"), 1);
    }

    /*查询待收货订单*/
    @GetMapping("/notake")
    public RpsMsg getNoTake(HttpSession session) {
        return orderService.getOrder((Long) session.getAttribute("phone"), 2);
    }

    /*查询已收货订单*/
    @GetMapping("/over")
    public RpsMsg getOver(HttpSession session) {
        return orderService.getOrder((Long) session.getAttribute("phone"), 3);
    }


}
