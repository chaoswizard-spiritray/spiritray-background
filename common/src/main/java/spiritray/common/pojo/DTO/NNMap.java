package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:NNMap
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/11/4 18:02
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class NNMap {
    private Integer key;
    private Integer value;
}
