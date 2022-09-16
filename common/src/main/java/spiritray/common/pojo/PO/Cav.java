package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Cav
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/27 8:12
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Cav {
    private String commodityId;
    private long attributeId;
    private String attributeName;
    private String attributeValue;
}
