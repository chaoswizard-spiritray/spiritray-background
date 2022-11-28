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
 * 当用户位于消息标签栏时会进行注册，用于实时监听消息标签栏中信息变化
 *
 * @Date:2022/11/26 10:53
 * @Author:灵@email
 */
@ServerEndpoint("/websocket/consumer/home/{consumerId}")
@Component
public class ConsumerMsgHomeSocket {

    /*
     * 因为ServerEndpoint无法通过spring进行注入，所以我们需要手动通过Spring上下文进行注入
     * */

    private ConcurrentHashMap<Long, Session> stayMsgHomeConsumer;//身份与会话的映射表

    private Session session;//这个类是多例的，因为每次连接都会创建一个对象，我们移除映射表时需要使用这个引用。

    /*请求连接时*/
    @OnOpen
    public void onOpen(@PathParam("consumerId") Long consumerId, Session session) {
        //获取应用级别的会话关系
        stayMsgHomeConsumer = SpringUtil.getBean("stayMsgHomeConsumer");
        //将当前会话保存
        this.session = session;
        stayMsgHomeConsumer.put(consumerId, session);
    }

    /*连接关闭前会触发*/
    @OnClose
    public void onClose() {
        //从映射表中移除会话
        stayMsgHomeConsumer.forEach((k, v) -> {
            if (stayMsgHomeConsumer.get(k).equals(this.session)) {
                stayMsgHomeConsumer.remove(k);
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
