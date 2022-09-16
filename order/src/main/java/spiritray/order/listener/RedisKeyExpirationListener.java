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
import spiritray.order.mapper.OrderDetailMapper;

import java.util.Map;

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

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /*监听过期key,message就是key*/
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String key = message.toString();
        String orderNo = key.substring(0, 36);
        int odId = Integer.parseInt(key.substring(36, key.length()));
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
        }
    }
}
