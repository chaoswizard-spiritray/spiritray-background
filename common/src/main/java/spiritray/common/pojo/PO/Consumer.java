package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Consumer
 * Package:spriritray.consumer.pojo.PO
 * Description:
 * 买家类
 *
 * @Date:2022/4/12 18:28
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Consumer {
    private String consumerHead;//头像
    private String consumerNickname;//昵称
    private long consumerPhone;//电话
    private byte consumerSex;//性别
    private String consumerPassword;//密码
    private byte isEnter=0;//是否入驻为商家,0为未入驻，1为已入驻,该字段并不在数据库中存在
}
