package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:AppPayRps
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/6/19 10:34
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AppPayRps {
    private int code;
    private String data;
    private String msg;
}
