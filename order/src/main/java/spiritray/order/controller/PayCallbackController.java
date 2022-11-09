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

    private final String Order_KEY_PREFIX = "order";//redis订单细节编号为key的前缀


    /*支付成功回调接口*/
    @PostMapping("/app")
    public RpsMsg payAppCallBack(String cpi, int code) {
        if (code == 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Cpi cpi1 = JSON.parseObject(cpi, Cpi.class);
                    //更新订单状态
                    //先判断支付时是单个商品支付的还是多个商品一起支付的,如果是多个商品支付的，外部支付订单号就是36的长度。如果是单个商品支付的，长度就大于36位，使用的是订单细节编号
                    if (cpi1.getPayNo().length() == 36) {
                        //如果是主订单号作为外部支付单号，就需要删除redis中所有缓存的未付款订单细节信息
                        //删除redis中的订单,指定前缀的订单
                        redisTemplate.delete(redisTemplate.keys(Order_KEY_PREFIX + cpi1.getPayNo() + "*"));
                        //改变所有订单细节编号的支付状态
                        orderDetailMapper.updateDetailByOrderNum(cpi1.getPayNo(), 1);
                        //将该订单下的所有信息插入支付数据
                        try {
                            cpiMapper.insertCpis(cpi1);
                        } catch (Exception e) {

                        }
                    } else {
                        //如果是单个商品进行支付，那么就删除指定订单即可
                        //删除指定key
                        redisTemplate.delete(Order_KEY_PREFIX + cpi1.getPayNo());
                        //改变指定订细节支付状态
                        orderDetailMapper.updateDetailStateById(cpi1.getPayNo().substring(0, 36), Integer.parseInt(cpi1.getPayNo().substring(36, cpi1.getPayNo().length())), 1);
                        cpi1.setCpiId(cpi1.getPayNo());
                        //插入支付记录
                        cpiMapper.insertCpi(cpi1);
                    }
                }
            }).start();
        }
        //返回数据
        return new RpsMsg().setStausCode(200).setData("SUCCESS");
    }

    /*退款回调接口*/
    @PostMapping("/back")
    public RpsMsg backCallback(String pbi, int code) {
        //保存信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                Pbi pbi1 = JSON.parseObject(pbi, Pbi.class);
                //如果退款成功先将退款信息保存起来
                if (code == 1) {
                    pbiMapper.insertPbi(pbi1);
                } else if (code == -1) {
                    //如果退款失败
                    //将任务保存到定时轮询退款任务中,会重新执行退款任务
                    SSMap ssMap = new SSMap();
                    ssMap.setAttributeValue(pbi1.getPbiId().substring(0, 36)).setAttributeName(pbi1.getPbiId().substring(36, pbi1.getPbiId().length()));
                    backFail.add(ssMap);
                }
            }
        }).start();
        return new RpsMsg().setStausCode(200).setData("SUCCESS");
    }

}
