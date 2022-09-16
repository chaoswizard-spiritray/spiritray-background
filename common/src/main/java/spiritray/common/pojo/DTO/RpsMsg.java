package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:RpsMsg
 * Package:spriritray.consumer.pojo.DAO
 * Description:
 * 普遍的前端响应信息
 *
 * @Date:2022/4/13 11:55
 * @Author:灵@email
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RpsMsg {
    private int stausCode;//响应状态码
    private String msg;//响应的提示信息
    private Object data;//响应时可能携带的数据，是一个对象，具体类型是什么前后端具体决定，传输是会转换为JSON字符串
}
