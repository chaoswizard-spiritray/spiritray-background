package spiritray.order.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.OrderDetail;
import spiritray.order.mapper.OrderDetailMapper;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ClassName:StoreOrderController
 * Package:spiritray.order.controller
 * Description:
 * 店铺订单管理
 * https://blog.csdn.net/web15085181368/article/details/124319173 mybatis遍历map集合取值
 *
 * @Date:2022/11/20 15:52
 * @Author:灵@email
 */
@RestController
@RequestMapping("/order/store")
public class StoreOrderController {
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /*检测指定店铺是否有未完成-分别是已付款到未确认收货之间状态的的订单*/
    @GetMapping("/check/over/{storeId}")
    public RpsMsg checkStoreNoneNoOverOrder(@PathVariable String storeId) {
        if (orderDetailMapper.selectCountNoOverOrder(storeId) == 0) {
            //如果没有未完成的订单就返回true
            return new RpsMsg().setStausCode(200).setData(true);
        } else {
            return new RpsMsg().setStausCode(200).setData(false);
        }
    }

    /*获当前会话的店铺信息
     * @param session 会话
     * @param keyWord 订单编号或者关键字
     * @param conditions 筛选条件 List
     *  condition:
     *  如果是未付款、未发货
     *  -recentTime:时间，不限：null，最近多少天：Integer
     *  -totalMoney：总计价格，不限：-1，上界值
     *  -addressMsg:收货地址，不限：null, 或者String
     *  -takePhone:收货电话，不限：null，或者Long
     *  如果是已发货额外
     *  -logisticsNo：物流单号
     *  如果是已收货额外
     *  -
     * @param reorder:排序
     * attributeName:
     *   -startTime:订单发起时间 默认
     *   -endTime:订单完成时间
     *   -totalMomoney:总计费用
     * attributeValue: ASC-升序，DESC-降序
     * */
    @GetMapping("/data/{type}/{pageNo}/{pageNum}")
    public RpsMsg getStoreOrder(HttpSession session, String param, @PathVariable Integer type, @PathVariable Integer pageNo, @PathVariable Integer pageNum) {
        List<Map> params;
        Map map = new HashMap();
        if (param != null) {
            //解析数据
            params = JSON.parseArray(param).toJavaList(Map.class);
            //转换为map便于取数据
            params.stream().peek(s -> map.putAll(s)).count();
        }
        //获取数据
        return new RpsMsg().setStausCode(200).setData(orderDetailMapper.selectStoreOrderDetailByParam((String) session.getAttribute("storeId"), type, map));
    }

    /**
     * 商家端获取筛选订单数据集合：订单价格区间，收货地址，收货电话，物流单号
     */
    @GetMapping("/data/{type}")
    public RpsMsg getConditionDataSet(@PathVariable Integer type, HttpSession session) {
        //获取订单细节信息
        List<OrderDetail> orderDetails = orderDetailMapper.selectOrderDetailConditionByType((String) session.getAttribute("storeId"), type);
        //判断是否存在订单
        if (orderDetails == null || orderDetails.size() == 0) {
            return new RpsMsg().setStausCode(200);
        }
        //因为数组已经按照总计费用排好序
        List<Float> totalMoney = new ArrayList<>();
        //计算平均值
        final Float average = new Float(orderDetails.stream().mapToDouble(OrderDetail::getTotalAmount).average().orElse(0D));
        //保存金额范围
        totalMoney.add((float) Math.floor(orderDetails.get(0).getTotalAmount()));
        totalMoney.add((float) Math.floor(average));
        totalMoney.add((float) Math.ceil(orderDetails.get(orderDetails.size() - 1).getTotalAmount()));
        //获取地址
        List<Address> addresses = orderDetails.stream().map(OrderDetail::getAddressMsg).map(s -> {
            return JSONUtil.toBean(s, Address.class);
        }).collect(Collectors.toList());
        //提取收货地址、收货电话、物流单号
        List<String> resultAddress = addresses.stream().map(Address::getAddress).distinct().collect(Collectors.toList());
        List<Long> takePhones = addresses.stream().map(Address::getTakePhone).distinct().collect(Collectors.toList());
        List<String> logisticsNos = orderDetails.stream().map(OrderDetail::getLogisticsNo).filter(s -> s != null).collect(Collectors.toList());
        //封装数据
        Map<String, Object> result = new HashMap<>();
        result.put("orderTotalMoney", totalMoney);
        result.put("addresses", resultAddress);
        result.put("takePhones", takePhones);
        if (logisticsNos.size() > 0) {
            result.put("logisticsNos", logisticsNos);
        }
        return new RpsMsg().setMsg("查询成功").setStausCode(200).setData(result);
    }

    /*指定订单发货*/
    @PutMapping("/trans")
    public RpsMsg putOrderTrans(String orderNumber, Integer odId, String logisticsNo, HttpSession session) {
        //先查询订单
        OrderDetail orderDetail;
        try {
            orderDetail = orderDetailMapper.selectOrderDetailById(orderNumber, odId);
        } catch (Exception e) {
            return new RpsMsg().setStausCode(300).setMsg("订单不存在");
        }
        RpsMsg rpsMsg = checkOrderTrans(orderDetail, (String) session.getAttribute("storeId"));
        if (rpsMsg == null) {
            try {
                int row = orderDetailMapper.updateOrderDetailLogisticsNo(orderNumber, odId, (String) session.getAttribute("storeId"), logisticsNo);
                if (row == 1) {
                    return new RpsMsg().setStausCode(200).setMsg("发货成功");
                }
                if (row == 0) {
                    orderDetail = orderDetailMapper.selectOrderDetailById(orderNumber, odId);
                    rpsMsg = checkOrderTrans(orderDetail, (String) session.getAttribute("storeId"));
                    if (rpsMsg == null) {
                        return new RpsMsg().setMsg("系统异常，发货失败").setStausCode(300);
                    }
                }
            } catch (Exception e) {
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
            }
        }
        return rpsMsg;
    }

    private RpsMsg checkOrderTrans(OrderDetail orderDetail, String storeId) {
        if (orderDetail == null) {
            //如果订单已经被取消
            return new RpsMsg().setStausCode(300).setMsg("订单不存在");
        }
        if (orderDetail.getLogisticsNo() != null) {
            return new RpsMsg().setStausCode(300).setMsg("订单已发货");
        }
        if (orderDetail.getState() != 1 || !orderDetail.getStoreId().equals(storeId)) {
            return new RpsMsg().setMsg("订单不合法").setStausCode(300);
        }
        return null;
    }


}
