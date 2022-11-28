package spiritray.plant.msg;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName:ConsumerMsgSocket
 * Package:spiritray.plant.msg
 * Description:
 * 当用户一进入应用模块时就连接，用户监听信息变化，当用户不在消息标签栏时，同样能够通知消息有变化
 *
 * @Date:2022/11/26 10:53
 * @Author:灵@email
 */
@ServerEndpoint("/websocket/consumer/{consumerId}")
@Component
public class ConsumerMsgSocket {

    /*
     * 因为ServerEndpoint无法通过spring进行注入，所以我们需要手动通过Spring上下文进行注入
     * */

    private ConcurrentHashMap<Long, Session> consumerPWMap;//身份与会话的映射表

    private Session session;//这个类是多例的，因为每次连接都会创建一个对象，我们移除映射表时需要使用这个引用。

    /*请求连接时*/
    @OnOpen
    public void onOpen(@PathParam("consumerId") Long consumerId, Session session) {
        //获取应用级别的会话关系
        consumerPWMap = SpringUtil.getBean("consumerPWMap");
        //将当前会话保存
        this.session = session;
        consumerPWMap.put(consumerId, session);
    }

    /*连接关闭前会触发*/
    @OnClose
    public void onClose() {
        //从映射表中移除会话
        consumerPWMap.forEach((k, v) -> {
            if (consumerPWMap.get(k).equals(this.session)) {
                consumerPWMap.remove(k);
            }
        });
        try {
            session.close();
        } catch (IOException e) {
            System.out.println("关闭异常");
        }
    }

    /*该方法我们只用来进行*/
    @OnMessage
    public void onMessage(String senderInfo, Session session) {
        //由于我们需要预存消息，所以不在这个类中进行消息发送监听，我们使用单独消息发送Controller进行处理，然后使用session进行消息推送。
    }

}
