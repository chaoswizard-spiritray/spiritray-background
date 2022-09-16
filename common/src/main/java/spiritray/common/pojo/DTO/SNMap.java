package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:SNMap
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/14 19:40
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SNMap {
    private String key;
    private long value;
}
