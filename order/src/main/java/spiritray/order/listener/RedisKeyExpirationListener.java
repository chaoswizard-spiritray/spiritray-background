package spiritray.order.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Msg;
import spiritray.common.tool.SystemMsgNotice;
import spiritray.order.mapper.OrderDetailMapper;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * ClassName:RedisKeyExpirationListener
 * Package:spiritray.order.listener
 * Description:
 *
 * @Date:2022/6/19 18:50
 * @Author:灵@email
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private HttpHeaders httpHeaders;

    private final String Order_KEY_PREFIX = "order";//redis订单细节编号为key的前缀

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /*监听过期key,message就是key*/
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        //先过滤一下key
        if (key.length() < 37 || key.indexOf(Order_KEY_PREFIX) != 0) {
            return;
        }
        //提取订单编号
        int len = Order_KEY_PREFIX.length();
        String orderNo = key.substring(len, len + 36);
        int odId = Integer.parseInt(key.substring(len + 36, key.length()));
        //先判断订单状态
        int state = orderDetailMapper.selectDetailStateById(orderNo, odId);
        if (state == 0) {
            //修改商品数量
            Map param = orderDetailMapper.selectOrderSkuByOrderId(orderNo, odId);
            MultiValueMap multiValueMap = new LinkedMultiValueMap();
            multiValueMap.add("commodityId", param.get("commodityId"));
            multiValueMap.add("skuValue", param.get("skuValue"));
            multiValueMap.add("num", param.get("num"));
            HttpEntity httpEntity = new HttpEntity(multiValueMap, httpHeaders);
            restTemplate.exchange("http://localhost:8081/sku/add", HttpMethod.PUT, httpEntity, RpsMsg.class);
            //删除订单细节记录
            orderDetailMapper.updateDetailDeleteById(orderNo, odId);
            //通知买家订单取消
            SystemMsgNotice.notieMsg(restTemplate, new Msg()
                    .setMsg("您的订单:" + orderNo + odId + "已超时自动取消")
                    .setMsgId(String.valueOf(UUID.randomUUID()))
                    .setMsgType("text")
                    .setReceiverRole(1)
                    .setSender(0L)
                    .setSenderRole(0)
                    .setSendDate(new Timestamp(new Date().getTime()))
                    .setReceiver(orderDetailMapper.selectOrderDetailInfo(orderNo, odId).getConsumerPhone()));
        }
    }
}
