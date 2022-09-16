package spiritray.order.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Cpi;
import spiritray.common.pojo.PO.OrderDetail;
import spiritray.common.pojo.PO.Pbi;
import spiritray.order.mapper.CpiMapper;
import spiritray.order.mapper.OrderDetailMapper;
import spiritray.order.mapper.PbiMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:PayCallbackController
 * Package:spiritray.order.controller
 * Description:
 *
 * @Date:2022/6/19 11:21
 * @Author:灵@email
 */
@RestController
@RequestMapping("/pay/callback")
public class PayCallbackController {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CpiMapper cpiMapper;

    @Autowired
    private PbiMapper pbiMapper;

    @Autowired
    @Qualifier("backFail")
    private List backFail;

    @Autowired
    private RedisTemplate redisTemplate;


    /*支付成功回调接口*/
    @PostMapping("/app")
    public RpsMsg payAppCallBack(String cpi, int code) {
        System.out.println(cpi);
        if (code == 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Cpi cpi1 = JSON.parseObject(cpi, Cpi.class);
                    //更新订单状态
                    if (cpi1.getCpiId().length() == 36) {
                        //删除redis中的订单,指定前缀的订单
                        redisTemplate.delete(redisTemplate.keys(cpi1.getCpiId() + "*"));
                        //改变所有订单细节编号的支付状态
                        orderDetailMapper.updateDetailByOrderNum(cpi1.getCpiId(), 1);
                    } else {
                        //删除指定key
                        redisTemplate.delete(cpi1.getCpiId());
                        //改变指定订细节支付状态
                        orderDetailMapper.updateDetailStateById(cpi1.getCpiId().substring(0, 36), Integer.parseInt(cpi1.getCpiId().substring(36, cpi1.getCpiId().length())), 1);
                    }
                    //插入支付记录
                    cpiMapper.insertCpi(cpi1);
                }
            }).start();
        }
        //返回数据
        return new RpsMsg().setStausCode(200).setData("SUCCESS");
    }

    /*退款回调接口*/
    @PostMapping("/back")
    public RpsMsg backCallback(String pbi, int code) {
        //如果退款成功
        if (code == 1) {
            //保存信息
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Pbi pbi1 = JSON.parseObject(pbi, Pbi.class);

                }
            }).start();
        } else {
            //将任务保存到定时轮询任务中
            SSMap ssMap = new SSMap();
            ssMap.setAttributeValue("http://localhost:8082/pay/callback/back").setAttributeName(pbi);
            backFail.add(ssMap);
        }
        return new RpsMsg().setStausCode(200).setData("SUCCESS");
    }

}
