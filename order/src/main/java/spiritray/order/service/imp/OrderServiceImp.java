package spiritray.order.service.imp;

import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.BO.CheckOrderInfo;
import spiritray.common.pojo.DTO.OrderBeforeCommodity;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.Order;
import spiritray.common.pojo.PO.OrderDetail;
import spiritray.order.mapper.OrderDetailMapper;
import spiritray.order.mapper.OrderMapper;
import spiritray.order.service.OrderService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName:OrderServiceImp
 * Package:spiritray.order.service.imp
 * Description:
 *
 * @Date:2022/6/17 14:33
 * @Author:灵@email
 */
@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    @Qualifier("orderTokens")
    private Map map;

    @Autowired
    @Qualifier("threadPool")
    private ThreadPoolExecutor threadPoolExecutor;//自定义线程池

    @Autowired
    @Qualifier("transferFail")
    private List transferFail;//转账失败集合


    @Autowired
    private HttpHeaders headers;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private final String CONSUMER_URL = "http://localhost:8080";

    private final String SELLER_URL = "http://localhost:8081";

    private final String ORDER_URL = "http://localhost:8082";

    private final String PLANT_URL = "http://localhost:8083";

    private final String Order_KEY_PREFIX = "order";//redis订单细节编号为key的前缀

    @Override
    public RpsMsg generateOrderToken() {
        //生成一个UUID
        String orderToken = UUID.randomUUID().toString();
        //添加到redis
        redisTemplate.opsForSet().add("orderToken", orderToken);
        //添加定时检测到集合
        map.put(orderToken, System.currentTimeMillis() + 600000);
        //返回信息
        return new RpsMsg().setData(orderToken).setStausCode(200);
    }

    @Override
    public RpsMsg generateOrderAndDetail(List<OrderBeforeCommodity> commodities, Address address, String orderId, int payCate, long comsumerPhone, String jwt) {
        //验证令牌是否存在
        if (redisTemplate.opsForSet().isMember("orderToken", orderId)) {
            //存在就删除redis订单令牌
            redisTemplate.opsForSet().remove("orderToken", orderId);
        } else {
            //不存在就返回错误
            return new RpsMsg().setStausCode(300).setMsg("无效订单");
        }
        //先检测下商品是否都在售
        if(!checkCommodityState(commodities)){
            //如果没有在售
            return new RpsMsg().setStausCode(300).setMsg("无法下单，存在商品已下架");
        }
        //获取有效地址信息
        headers.add("jwt", jwt);
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
        RpsMsg tempMsg=restTemplate.exchange(CONSUMER_URL + "/consumer/info/addresses", HttpMethod.GET, httpEntity, RpsMsg.class).getBody();
        List<Address> checkAddresses = (List<Address>)tempMsg.getData();
        checkAddresses = JSONObject.parseArray(JSON.toJSONString(checkAddresses)).toJavaList(Address.class);
        boolean isAccess = false;
        for (int i = 0; i < checkAddresses.size(); i++) {
            if (checkAddresses.get(i).getAddressId().equals(address.getAddressId())) {
                isAccess = true;
                break;
            }
        }
        if (!isAccess) {
            //如果地址信息不存在
            return new RpsMsg().setStausCode(300).setMsg("请选择已有地址信息");
        }
        //封装参数
        List<SSMap> checkParam = new ArrayList<>();
        List<Integer> commodityNums = new ArrayList<>();
        for (OrderBeforeCommodity commodity : commodities) {
            SSMap ssMap = new SSMap();
            ssMap.setAttributeName(commodity.getCommodityId()).setAttributeValue(commodity.getSku().getSkuValue());
            checkParam.add(ssMap);
            commodityNums.add(commodity.getCommodityNum());
        }
        multiValueMap.add("commodities", JSON.toJSONString(checkParam));
        multiValueMap.add("commodityNums", JSON.toJSONString(commodityNums));
        httpEntity = new HttpEntity(multiValueMap, headers);
        //获取商品规格信息
        ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(SELLER_URL + "/sku/checkorder", HttpMethod.PUT, httpEntity, RpsMsg.class);
        List<CheckOrderInfo> checkOrderInfos = JSONObject.parseArray(JSON.toJSONString(responseEntity.getBody().getData())).toJavaList(CheckOrderInfo.class);
        if (checkOrderInfos.size() != commodities.size()) {
            return new RpsMsg().setMsg("商品信息不存在");
        }
        //再验证下商品是否都在售
        if(!checkCommodityState(commodities)){
            //如果没有在售
            return new RpsMsg().setStausCode(300).setMsg("无法下单，存在商品已下架");
        }
        //减少商品指定规格数量
        responseEntity = restTemplate.exchange(SELLER_URL + "/sku/sub", HttpMethod.PUT, httpEntity, RpsMsg.class);
        if (responseEntity.getStatusCode().is5xxServerError()) {
            return new RpsMsg().setStausCode(300).setMsg("所选商品数量不足");
        }
        if (responseEntity.getBody().getStausCode() != 200) {
            return responseEntity.getBody();
        } else {
            float totalFee = 0;//所有商品总计费用
            //生成订单细节
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (int i = 0; i < checkOrderInfos.size(); i++) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setStoreId(checkOrderInfos.get(i).getStoreId()).setCommodityId(checkOrderInfos.get(i).getCommodityId())
                        .setOrderNumber(orderId).setOdId(i).setAddressMsg(JSONObject.toJSONString(address))
                        .setSkuValue(checkOrderInfos.get(i).getSkuValue()).setSkuMap(checkOrderInfos.get(i).getSkuMap());
                //调整数目计算每个商品小计
                for (OrderBeforeCommodity commodity : commodities) {
                    if (commodity.getCommodityId().equals((checkOrderInfos.get(i)).getCommodityId())) {
                        orderDetail.setCommodityNum(commodity.getCommodityNum());
                        float mallSum = checkOrderInfos.get(i).getSkuPrice() * commodity.getCommodityNum() - commodity.getShipping();
                        totalFee += mallSum;
                        orderDetail.setTotalAmount(mallSum);
                    }
                }
                orderDetails.add(orderDetail);
                //以订单编号为id设置redis过期变量,用于定时取消订单,下面我们就不进行redis预减库存了,因为在支付前会进行数量减少
                redisTemplate.opsForValue().set(Order_KEY_PREFIX + orderId + i, 1);
                redisTemplate.expire(Order_KEY_PREFIX + orderId + i, 10, TimeUnit.MINUTES);
                //将商品添加到hash表中进行商品数量预减，因为使用hash进行存储，可以进行自增自减，如果无法自增情况，我们需要通过redis事务或者通过redis变量做锁的情况进行处理
                //redisTemplate.opsForHash().increment("preReduction", orderDetail.getCommodityId(), orderDetail.getCommodityNum());//商品出售数量
            }
            //生成订单
            Order order = new Order().setOrderNumber(orderId).setConsumerPhone(comsumerPhone).setTotalAmount(totalFee);
            //信息保存
            if (saveOrderInfo(order, orderDetails)) {
                //返回信息
                return new RpsMsg().setStausCode(200).setData(new SSMap(orderId, order.getTotalAmount() + "")).setMsg("下单成功");
            } else {
                //返回下单失败
                return new RpsMsg().setMsg("下单失败").setStausCode(200);
            }
        }
    }

    //批量检测商品状态是否都是在售中
    private boolean checkCommodityState(List<OrderBeforeCommodity> commodities) {
        try {
            List<String> commodityIds = commodities.stream().map(OrderBeforeCommodity::getCommodityId).collect(Collectors.toList());
            RpsMsg rpsMsg = restTemplate.getForObject(SELLER_URL + "/commodity/check/state?commodityIds=" + JSONUtil.toJsonStr(commodityIds), RpsMsg.class);
            if (rpsMsg.getData() == null) {
                return false;
            } else {
                return (Boolean) rpsMsg.getData() ? true : false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public RpsMsg queryStateByOrderNumber(String orderNumber) {
        if (orderNumber.length() == 36) {
            //如果是总订单号
            return new RpsMsg().setMsg("查询成功").setStausCode(200).setData(orderDetailMapper.selectDetailStateByOrderNumber(orderNumber));
        } else {
            //如果是订单细节编号
            return new RpsMsg().setMsg("查询成功").setStausCode(200)
                    .setData(orderDetailMapper.selectDetailStateById(orderNumber.substring(0, 36), Integer.parseInt(orderNumber.substring(36, orderNumber.length()))));
        }
    }

    @Override
    public RpsMsg getOrder(long phone, int state) {
        return new RpsMsg().setStausCode(200).setData(orderDetailMapper.selectOrderDetailByPhoneAndState(phone, state));
    }

    @Override
    public RpsMsg modifyOrderDetailAddressByOrderNumberAndOdId(String orderNumber, int odId, String address, HttpServletRequest request) {
        //先验证收货地址是否合法
        String jwt = request.getHeader("jwt");
        Long phone = (Long) request.getSession().getAttribute("phone");
        Address consumerAddress = JSONObject.parseObject(address, Address.class);
        headers.add("jwt", jwt);
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
        List<Address> checkAddresses = (List<Address>) restTemplate.exchange(CONSUMER_URL + "/consumer/info/addresses", HttpMethod.GET, httpEntity, RpsMsg.class).getBody().getData();
        checkAddresses = JSONObject.parseArray(JSON.toJSONString(checkAddresses)).toJavaList(Address.class);
        boolean isAccess = false;
        for (int i = 0; i < checkAddresses.size(); i++) {
            if (checkAddresses.get(i).getAddressId().equals(consumerAddress.getAddressId())) {
                isAccess = true;
                break;
            }
        }
        if (!isAccess) {
            //如果地址信息不存在
            return new RpsMsg().setStausCode(300).setMsg("请选择已有地址信息");
        }
        //修改订单细节记录的地址
        try {
            if (orderDetailMapper.updateDetailAddress(address, phone, orderNumber, odId) == 1) {
                return new RpsMsg().setStausCode(200).setMsg("收货地址修改成功");
            } else {
                return new RpsMsg().setStausCode(300).setMsg("修改失败，请稍后再试");
            }
        } catch (Exception e) {
            return new RpsMsg().setStausCode(300).setMsg("修改失败，请稍后再试");
        }
    }

    @SneakyThrows
    @Transactional(rollbackFor = Exception.class)
    @Override
    public RpsMsg chanelOrderDetail(HttpServletResponse response, String orderNumber, int odId, long phone, String jwt) throws Exception {
        //先删除指定订单细节记录
        int rowNum = orderDetailMapper.updateDetailDeleteByIdAndPhone(orderNumber, odId, phone);
        if (rowNum != 1) {
            return new RpsMsg().setMsg("系统繁忙").setStausCode(300);
        }
        //修改商品数量
        Map param = orderDetailMapper.selectOrderSkuByOrderId(orderNumber, odId);
        MultiValueMap multiValueMap1 = new LinkedMultiValueMap();
        multiValueMap1.add("commodityId", param.get("commodityId"));
        multiValueMap1.add("skuValue", param.get("skuValue"));
        multiValueMap1.add("num", param.get("num"));
        HttpEntity httpEntity = new HttpEntity(multiValueMap1, new HttpHeaders());
        RpsMsg rpsMsg1 = restTemplate.exchange("http://localhost:8081/sku/add", HttpMethod.PUT, httpEntity, RpsMsg.class).getBody();
        //如果增加失败直接回滚
        if (rpsMsg1.getStausCode() == 300) {
            throw new Exception();
        }
        //如果修改成功,调用退款接口
        headers.add("jwt", jwt);
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("orderNumber", orderNumber);
        multiValueMap.add("odId", odId);
        HttpEntity entity = new HttpEntity(multiValueMap, headers);
        ResponseEntity responseEntity = restTemplate.exchange(ORDER_URL + "/order/pay/detail/back", HttpMethod.POST, entity, RpsMsg.class);
        //如果请求存在故障，抛出异常
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            //先将取消订单失败信息反馈给用户
            try {
                response.getWriter().write(JSON.toJSONString(new RpsMsg().setStausCode(300).setMsg("系统繁忙,请稍后再试")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //然后抛出异常，回滚修改
            throw new IllegalArgumentException();
        }
        //如果服务响应，判断响应的状态
        RpsMsg rpsMsg = (RpsMsg) responseEntity.getBody();
        if (rpsMsg.getStausCode() != 200) {
            try {
                response.getWriter().write(JSON.toJSONString(new RpsMsg().setStausCode(300).setMsg("系统繁忙,请稍后再试")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //然后抛出异常，回滚修改
            throw new IllegalArgumentException();
        } else {
            return new RpsMsg().setStausCode(200).setMsg("取消订单成功,退款稍后到账");
        }
    }

    @Override
    public RpsMsg modifyOrderStateToPublish(String orderNumber, Integer odId, Long phone) {
        //先验证买家是否订单细节记录是否存在
        Integer state = orderDetailMapper.selectOrderDetailStateByPhoneAndOrderNumber(orderNumber, odId, phone);
        if (state == null || state != 3) {
            //如果订单细节不存在或者订单状态不是已收货未评价就返回错误信息
            return new RpsMsg().setMsg("无效订单").setStausCode(300);
        } else {
            //否则就更新订单细节状态
            if (orderDetailMapper.updateDetailStateById(orderNumber, odId, 4) == 1) {
                return new RpsMsg().setMsg("修改成功").setStausCode(200);
            } else {
                return new RpsMsg().setMsg("系统繁忙").setStausCode(300);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RpsMsg suerOrderdetailOver(String orderNumber, Integer odId, HttpServletResponse response) {
        //修改订单状态以及结束日期
        int row = orderDetailMapper.updateDetailStateAndOverDateById(orderNumber, odId, new Timestamp(new Date().getTime()));
        if (row == 0) {
            return new RpsMsg().setStausCode(300).setMsg("无效订单");
        } else {
            //进行转账
            MultiValueMap multiValueMap = new LinkedMultiValueMap();
            multiValueMap.add("orderNumber", orderNumber);
            multiValueMap.add("odId", odId);
            HttpEntity entity = new HttpEntity(multiValueMap, headers);
            ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(ORDER_URL + "/order/pay/detail/trans", HttpMethod.POST, entity, RpsMsg.class);
            //如果请求存在故障，抛出异常
            if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody().getStausCode() == 300) {
                throw new IllegalArgumentException();
            }
            if (responseEntity.getBody().getStausCode() == 200) {
                return new RpsMsg().setStausCode(200).setMsg("确认收货成功");
            }
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
        }
    }

    @Transactional(rollbackFor = IllegalArgumentException.class)
    boolean saveOrderInfo(Order order, List<OrderDetail> orderDetails) {
        //插入数据
        int i = orderMapper.insertOrder(order);
        int j = orderDetailMapper.insertOrderDetail(orderDetails);
        //判断插入状况
        if (i > 0 && j == orderDetails.size()) {
            //全部插入成功，返回成功
            return true;
        } else {
            //回滚数据
            throw new IllegalArgumentException();
        }
    }


}
