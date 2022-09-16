package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Attribute
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/26 14:03
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Attribute {
    private long attributeId;
    private String attributeName;
    private int isMul;
}
