package spiritray.order.controller;

import cn.hutool.core.thread.ThreadUtil;
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
import spiritray.common.pojo.BO.BackPayParam;
import spiritray.common.pojo.BO.WechatAppPayParam;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Cpi;
import spiritray.common.pojo.PO.Pbi;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:ThridPayController
 * Package:spiritray.order.controller
 * Description:这个类主要是用来模拟第三方支付，包括支付数据拉取和支付接口调用
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
        //保存交易订单信息，我们这里是进行模拟交易，保存订单，保存支付状态
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
        if (redisTemplate.opsForHash().hasKey("payTradeOrders", data.getAttributeName())) {
            if (payState == -1) {//取消订单
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());
                return new RpsMsg().setStausCode(200).setMsg("取消支付成功");
            } else {
                //验证密码
                if (password == "") {
                    return new RpsMsg().setStausCode(300).setMsg("密码错误");
                }
                //进行付款一系列操作后调用回调
                AliAppPayParam aliAppPayParam = (AliAppPayParam) redisTemplate.opsForHash().get("payTradeOrders", data.getAttributeName());
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());//移除订单信息，表示订单已经处理
                //调用回调接口
                MultiValueMap multiValueMap = new LinkedMultiValueMap();
                Cpi cpi = new Cpi();
                cpi.setAccaId(1).setPlantAccount(aliAppPayParam.getMch_id()).setPayMoney(Float.valueOf(aliAppPayParam.getTotal_fee()))
                        .setPayNo(aliAppPayParam.getOut_trade_no()).setPayDate(new Timestamp(new Date().getTime()));
                multiValueMap.add("cpi", cpi);
                multiValueMap.add("code", 1);
                HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
                int num = 0;
                //循环调用回调地址
                while (true) {
                    num++;
                    ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(aliAppPayParam.getNotify_url(), HttpMethod.POST, httpEntity, RpsMsg.class);
                    //如果回调失败就等待3s再次请求
                    if ((!responseEntity.getStatusCode().is2xxSuccessful()) || (!("SUCCESS").equals(responseEntity.getBody().getData().toString()))) {
                        ThreadUtil.sleep(3, TimeUnit.SECONDS);
                    } else {
                        return new RpsMsg().setStausCode(200).setMsg("支付成功");
                    }
                    //调用回调地址失败次数大于5,回滚支付数据,返回支付失败
                    if (num > 5) {
                        //回滚业务逻辑。。。。。。。。。
                        return new RpsMsg().setStausCode(300).setMsg("支付失败");
                    }
                }
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
                //验证密码
                if (password == "") {
                    return new RpsMsg().setStausCode(300).setMsg("密码错误");
                }
                //进行付款一系列操作后调用回调
                WechatAppPayParam wechatAppPayParam = (WechatAppPayParam) redisTemplate.opsForHash().get("payTradeOrders", data.getAttributeName());
                redisTemplate.opsForHash().delete("payTradeOrders", data.getAttributeName());//移除订单
                //调用回调接口
                MultiValueMap multiValueMap = new LinkedMultiValueMap();
                Cpi cpi = new Cpi();
                System.out.println(wechatAppPayParam);
                cpi.setAccaId(1).setPlantAccount(wechatAppPayParam.getMch_id()).setPayMoney(Float.valueOf(wechatAppPayParam.getTotal_fee()))
                        .setPayNo(wechatAppPayParam.getOut_trade_no()).setPayDate(new Timestamp(new Date().getTime()));
                multiValueMap.add("cpi", cpi);
                multiValueMap.add("code", 1);
                HttpEntity httpEntity = new HttpEntity(multiValueMap, headers);
                int num = 0;
                //循环调用回调地址
                while (true) {
                    num++;
                    ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(wechatAppPayParam.getNotify_url(), HttpMethod.POST, httpEntity, RpsMsg.class);
                    //如果回调失败就等待3s再次请求
                    if ((!responseEntity.getStatusCode().is2xxSuccessful()) || (!("SUCCESS").equals(responseEntity.getBody().getData().toString()))) {
                        ThreadUtil.sleep(3, TimeUnit.SECONDS);
                    } else {
                        return new RpsMsg().setStausCode(200).setMsg("支付成功");
                    }
                    if (num >= 5) {
                        return new RpsMsg().setStausCode(300).setMsg("支付失败");
                    }
                }
            }
        } else {
            return new RpsMsg().setStausCode(300).setMsg("支付对象不存在");
        }
    }

    /*退款调用地址*/
    @PutMapping("/back")
    public AppPayRps backOrder(String param, int accaId) {
        BackPayParam backPayParam = JSON.parseObject(param, BackPayParam.class);
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        Pbi pbi = new Pbi();
        pbi.setPayNo(backPayParam.getOutTradeNo())
                .setBackMoney(backPayParam.getMoney())
                .setBackStatus(1)
                .setAccaId(accaId)
                .setPbiId(backPayParam.getOutTradeRefundNo())
                .setPlantAccount(backPayParam.getMchId())
                .setReturnNo(String.valueOf(UUID.randomUUID()))
                .setStartDate(new Timestamp(new Date().getTime()))
                .setBackDate(new Timestamp(new Date().getTime() + 60000));
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
                    responseEntity = restTemplate.exchange(backPayParam.getNotifyUrl(), HttpMethod.POST, httpEntity, RpsMsg.class);
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
        return new AppPayRps().setCode(1).setMsg("退款请求成功");
    }

    /*转账调用地址*/
    @PutMapping("/transfer")
    public RpsMsg transfer(String param, int accaId) {

    return null;
    }

}
