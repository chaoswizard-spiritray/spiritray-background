package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:LSS
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/11/27 23:46
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class LSS {
    private Long phone;
    private String name;
    private String head;
}
