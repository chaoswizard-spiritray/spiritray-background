package spiritray.order.controller;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.BO.AliAppPayParam;
import spiritray.common.pojo.BO.AppPayRps;
import spiritray.common.pojo.BO.WechatAppPayParam;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Cpi;
import spiritray.common.pojo.PO.Pbi;

import java.util.UUID;

/**
 * ClassName:ThridPayController
 * Package:spiritray.order.controller
 * Description:
 *
 * @Date:2022/6/19 9:43
 * @Author:灵@email
 */
@RestController
@RequestMapping("/pay")
public class ThridPayController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private HttpHeaders headers;

    @Autowired
    private RestTemplate restTemplate;

    /*支付宝app支付数据拉取接口*/
    @PutMapping("/app/data/ali")
    public AppPayRps getAliPayData(String param) {
        //解析参数,body的信息就是外部订单号和总价
        AliAppPayParam aliAppPayParam = JSON.parseObject(param, AliAppPayParam.class);
        //保存交易订单信息
        redisTemplate.opsForHash().put("payTradeOrders", aliAppPayParam.getOut_trade_no(), aliAppPayParam);
        return new AppPayRps().setCode(1).setMsg("下单成功").setData(aliAppPayParam.getBody());
    }

    /*微信app支付数据拉取接口*/
    @PutMapping("/app/data/wechat")
    public AppPayRps getWechatPayData(String param) {
        //解析参数
        WechatAppPayParam wechatAppPayParam = JSON.parseObject(param, WechatAppPayParam.class);
        //保存交易订单信息
        redisTemplate.opsForHash().put("payTradeOrders", wechatAppPayParam.getOut_trade_no(), wechatAppPayParam);
        return new AppPayRps().setCode(1).setMsg("下单成功").setData(wechatAppPayParam.getBody());
    }

    /*支付宝app支付调用地址*/
    @PutMapping("/app/order/state/ali")
    public RpsMsg putAliPayOrder(int payState, String body, String password) {
        SSMap data = JSON.parseObject(body, SSMap.class);
        //验证订单是否存在
        System.out.println(data);
        if (redisTemplate.opsForHash().hasKey("payTradeOrders", data.getAttributeName())) {
            if (payState == -1) {//取消订单
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());
                return new RpsMsg().setStausCode(200).setMsg("取消支付成功");
            } else {
                //进行付款一系列操作后调用回调
                AliAppPayParam aliAppPayParam = (AliAppPayParam) redisTemplate.opsForHash().get("payTradeOrders", data.getAttributeName());
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());//移除订单信息，表示订单已经处理
                //调用回调接口
                System.out.println(aliAppPayParam);
                MultiValueMap multiValueMap = new LinkedMultiValueMap();
                Cpi cpi = new Cpi();
                cpi.setAccaId(1).setCpiId(aliAppPayParam.getOut_trade_no()).setPayMoney(aliAppPayParam.getTotal_fee())
                        .setPayNo(String.valueOf(UUID.randomUUID())).setPlantAccount(aliAppPayParam.getMch_id()).setYungouNo(String.valueOf(UUID.randomUUID()));
                multiValueMap.add("cpi", cpi);
                multiValueMap.add("code", 1);
                HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
                //异步回调
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean flag = true;
                        ResponseEntity<RpsMsg> responseEntity = null;
                        responseEntity = restTemplate.exchange(aliAppPayParam.getNotify_url(), HttpMethod.POST, httpEntity, RpsMsg.class);
//                        while (flag) {
//                            responseEntity = restTemplate.exchange(aliAppPayParam.getNotify_url(), HttpMethod.POST, httpEntity, RpsMsg.class);
//                            if (responseEntity.getStatusCode().is2xxSuccessful()) {
//                                if (responseEntity.getBody().getData().equals("SUCCESS")) {
//                                    flag = false;
//                                }
//                            }
//                            try {
//                                Thread.sleep(3000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
                    }
                }).start();
                //实时返回
                return new RpsMsg().setMsg("付款成功").setStausCode(200);
            }
        } else {
            return new RpsMsg().setStausCode(300).setMsg("支付对象不存在");
        }
    }

    /*微信app支付调用地址*/
    @PutMapping("/app/order/state/wechat")
    public RpsMsg putWecPayOrder(int payState, String body, String password) {
        SSMap data = JSON.parseObject(body, SSMap.class);
        //验证订单是否存在
        if (redisTemplate.opsForHash().hasKey("payTradeOrders", data.getAttributeName())) {
            if (payState == -1) {//取消订单
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());//移除订单
                return new RpsMsg().setStausCode(200).setMsg("订单取消成功");
            } else {
                //进行付款一系列操作后调用回调
                WechatAppPayParam wechatAppPayParam = (WechatAppPayParam) redisTemplate.opsForHash().get("payTradeOrders", data.getAttributeName());
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());//移除订单
                //调用回调接口
                MultiValueMap multiValueMap = new LinkedMultiValueMap();
                Cpi cpi = new Cpi();
                cpi.setAccaId(1).setCpiId(wechatAppPayParam.getOut_trade_no()).setPayMoney(wechatAppPayParam.getTotal_fee())
                        .setPayNo(String.valueOf(UUID.randomUUID())).setPlantAccount(wechatAppPayParam.getMch_id()).setYungouNo(String.valueOf(UUID.randomUUID()));
                multiValueMap.add("cpi", cpi);
                multiValueMap.add("code", 1);
                HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
                //异步回调
                new Thread(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        boolean flag = true;
                        ResponseEntity<RpsMsg> responseEntity = null;
                        responseEntity = restTemplate.exchange(wechatAppPayParam.getNotify_url(), HttpMethod.POST, httpEntity, RpsMsg.class);
//                        while (flag) {
//                            responseEntity = restTemplate.exchange(wechatAppPayParam.getNotify_url(), HttpMethod.POST, httpEntity, RpsMsg.class);
//                            if (responseEntity.getStatusCode().is2xxSuccessful()) {
//                                if (responseEntity.getBody().getData().equals("SUCCESS")) {
//                                    flag = false;
//                                }
//                            }
//                            Thread.sleep(5000);
//                        }
                    }
                }).start();
                //实时返回
                return new RpsMsg().setMsg("付款成功").setStausCode(200);
            }
        } else {
            return new RpsMsg().setStausCode(300).setMsg("支付对象不存在");
        }
    }


    /*退款调用地址*/
    @PutMapping("/back")
    public RpsMsg backOrder(String param, String backUrl) {
        Pbi pbi = JSON.parseObject(param, Pbi.class);
        pbi.setYungouNo(String.valueOf(UUID.randomUUID())).setPayNo(String.valueOf(UUID.randomUUID()));
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("pbi", pbi);
        multiValueMap.add("code", 1);
        HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
        //异步回调
        new Thread(new Runnable() {
            @SneakyThrows
            @Override
            public void run() {
                boolean flag = true;
                ResponseEntity<RpsMsg> responseEntity = null;
                while (flag) {
                    responseEntity = restTemplate.exchange(backUrl, HttpMethod.POST, httpEntity, RpsMsg.class);
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        if (responseEntity.getBody().getData().equals("SUCCESS")) {
                            flag = false;
                        }
                    }
                    Thread.sleep(3000);
                }
            }
        }).start();
        //实时返回
        return new RpsMsg().setMsg("退款中").setStausCode(200);
    }
}
