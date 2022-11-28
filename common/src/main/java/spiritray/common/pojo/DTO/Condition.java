package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ClassName:Condition
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/11/20 16:50
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Condition {
    private String name;
    private List<Object> values;
}
