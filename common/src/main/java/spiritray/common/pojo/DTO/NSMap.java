package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:NSMap
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/8 11:39
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class NSMap {
    private int key;
    private String value;
}
