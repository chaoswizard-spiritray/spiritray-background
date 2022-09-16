package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * ClassName:RspMsgAPI
 * Package:spiritray.common.pojo.DTO
 * Description:
 * 调用第三方接口返回值类型
 *
 * @Date:2022/4/18 10:48
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RspMsgAPI {
    private String msg;
    private boolean success;
    private int code;
    private Map data;
}
