package spiritray.order.service.imp;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import spiritray.common.pojo.BO.AliAppPayParam;
import spiritray.common.pojo.BO.CheckOrderInfo;
import spiritray.common.pojo.BO.WechatAppPayParam;
import spiritray.common.pojo.DTO.OrderBeforeCommodity;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.*;
import spiritray.order.mapper.OrderDetailMapper;
import spiritray.order.mapper.OrderMapper;
import spiritray.order.service.OrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private HttpHeaders headers;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private final String CONSUMER_URL = "http://localhost:8080";

    private final String SELLER_URL = "http://localhost:8081";

    private final String PLANT_URL = "http://localhost:8083";

    @Override
    public RpsMsg generateOrderToken() {
        //生成一个UUID
        String orderToken = String.valueOf(UUID.randomUUID());
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
        //获取有效地址信息
        headers.add("jwt", jwt);
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
        List<Address> checkAddresses = (List<Address>) restTemplate.exchange(CONSUMER_URL + "/consumer/info/addresses", HttpMethod.GET, httpEntity, RpsMsg.class).getBody().getData();
        checkAddresses = JSONObject.parseArray(JSON.toJSONString(checkAddresses)).toJavaList(Address.class);
        //获取有效支付方式
        List<AccountCategory> accountCategories = (List<AccountCategory>) restTemplate.exchange(PLANT_URL + "/plant/account/category", HttpMethod.GET, httpEntity, RpsMsg.class).getBody().getData();
        accountCategories = JSONObject.parseArray(JSON.toJSONString(accountCategories)).toJavaList(AccountCategory.class);
        boolean isAccess = false;
        for (int i = 0; i < checkAddresses.size(); i++) {
            if (checkAddresses.get(i).getAddressId().equals(address.getAddressId())) {
                isAccess = true;
                break;
            }
        }
        if (!isAccess) {
            //如果地址信息不存在
            return new RpsMsg().setStausCode(300).setMsg("地址信息被篡改");
        } else {
            //验证支付类型
            for (AccountCategory accountCategory : accountCategories) {
                if (accountCategory.accaId == payCate && accountCategory.isOpen == 0) {
                    isAccess = false;
                } else if (accountCategory.accaId == payCate && accountCategory.isOpen == 1) {
                    break;
                }
            }
            if (!isAccess) {
                return new RpsMsg().setStausCode(300).setMsg("无效支付方式");
            }
            //获取平台当前种类收款账户
            List<PlantAccount> accounts = (List<PlantAccount>) restTemplate.exchange(PLANT_URL + "/plant/account/useable/1", HttpMethod.GET, httpEntity, RpsMsg.class).getBody().getData();
            accounts = JSONObject.parseArray(JSON.toJSONString(accounts), PlantAccount.class);
            if (accounts == null || accounts.size() == 0) {
                return new RpsMsg().setStausCode(300).setMsg("平台暂时没有该类交易账户,请选择其它方式支付或者过段时间再下单");
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
                    redisTemplate.opsForValue().set(orderId + i, 1);
                    redisTemplate.expire(orderId + i, 10, TimeUnit.MINUTES);
                    //将商品添加到hash表中进行商品数量预减，因为使用hash进行存储，可以进行自增自减，如果无法自增情况，我们需要通过redis事务或者通过redis变量做锁的情况进行处理
                    //redisTemplate.opsForHash().increment("preReduction", orderDetail.getCommodityId(), orderDetail.getCommodityNum());//商品出售数量
                }
                //生成订单
                Order order = new Order().setOrderNumber(orderId).setConsumerPhone(comsumerPhone).setTotalAmount(totalFee);
                //信息保存
                if (saveOrderInfo(order, orderDetails)) {
                    //封装付款信息
                    Object payData;
                    if (payCate == 1) {
                        AliAppPayParam aliAppPayParam = new AliAppPayParam();
                        aliAppPayParam.setAttach(JSON.toJSONString(new SSMap(orderId, order.getTotalAmount() + "")))
                                .setBody(JSON.toJSONString(new SSMap(orderId, order.getTotalAmount() + "")))
                                .setMch_id(accounts.get(0).getAccountNo())
                                .setNotify_url("http://localhost:8082/pay/callback/app")
                                .setOut_trade_no(orderId)
                                .setSign(accounts.get(0).getAccountKey())
                                .setTotal_fee(order.getTotalAmount() + "");
                        payData = getPayData(1, aliAppPayParam);
                    } else {
                        WechatAppPayParam wechatAppPayParam = new WechatAppPayParam();
                        wechatAppPayParam.setApp_id(accounts.get(0).getAppId())
                                .setBody(JSON.toJSONString(new SSMap(orderId, order.getTotalAmount() + "")))
                                .setMch_id(accounts.get(0).getAccountNo())
                                .setNotify_url("http://localhost:8082/pay/callback/app")
                                .setOut_trade_no(orderId)
                                .setSign(accounts.get(0).getAccountKey());
                        payData = getPayData(2, wechatAppPayParam);
                    }
                    //返回信息
                    return new RpsMsg().setStausCode(200).setData(new SSMap(orderId, order.getTotalAmount() + "")).setMsg("下单成功");
                } else {
                    //返回下单失败
                    return new RpsMsg().setMsg("下单失败").setStausCode(200);
                }
            }
        }
    }

    @Override
    public RpsMsg orderPay(String orderNum, int odId) {
        return null;
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

    /*yungoos第三方支付信息拉取*/
    private Object getPayData(int cate, Object param) {
        //根据支付方式请求yungouos的支付接得到支付数据
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("param", param);
        HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
        if (cate == 1) {
            //请求支付宝支付数据
            return restTemplate.exchange("http://localhost:8082/pay/app/data/ali", HttpMethod.PUT, httpEntity, RpsMsg.class).getBody().getData();
        } else if (cate == 2) {
            //请求微信支付数据
            return restTemplate.exchange("http://localhost:8082/pay/app/data/wechat", HttpMethod.PUT, httpEntity, RpsMsg.class).getBody().getData();
        } else {
            return null;
        }
    }
}