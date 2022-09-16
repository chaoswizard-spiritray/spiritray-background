package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import spiritray.common.pojo.PO.CheckInfo;

/**
 * ClassName:CheckInfoExtend
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/14 16:05
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckInfoExtend extends CheckInfo {
    private String info;
    private int state;
}
