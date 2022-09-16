package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:SSMap
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/15 22:29
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SSMap {
    private String attributeName;
    private String attributeValue;
}
