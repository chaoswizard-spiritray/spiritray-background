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
import javax.servlet.http.HttpServletResponse;
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

    private final String Order_KEY_PREFIX = "order";//redis订单细节编号为key的前缀

    /*查询指定商品数组的过期时间*/
    @GetMapping("/overtime")
    public RpsMsg getOverTime(String orderDetailIds) {
        List<String> ids = JSON.parseArray(orderDetailIds).toJavaList(String.class);
        List<SNMap> data = new ArrayList<>();
        for (String id : ids) {
            SNMap snMap = new SNMap(id, redisTemplate.getExpire(Order_KEY_PREFIX + id));
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

    //修改当前用户未发货订单的收货地址
    @PutMapping("/notrans/address")
    public RpsMsg putOrderAddress(HttpServletRequest request, String address, String orderNumber, int odId) {
        //调用的修改订单服务
        return orderService.modifyOrderDetailAddressByOrderNumberAndOdId(orderNumber, odId, address, request);
    }

    /*取消未发货的订单*/
    @PutMapping("/notrans/chanel")
    public RpsMsg chanelOrderDetail(String orderNumber, int odId, HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        return orderService.chanelOrderDetail(response, orderNumber, odId, (Long) session.getAttribute("phone"), request.getHeader("jwt"));
    }


}
