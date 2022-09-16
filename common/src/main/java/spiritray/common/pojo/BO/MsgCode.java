package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:MsgCode
 * Package:spriritray.consumer.pojo.BO
 * Description:
 * 短信验证码信息
 *
 * @Date:2022/4/13 12:09
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MsgCode {
    private String code;//随机生成的验证码
    private long startT;//验证发送开始时间
    private int enableT;//有效时间
}
