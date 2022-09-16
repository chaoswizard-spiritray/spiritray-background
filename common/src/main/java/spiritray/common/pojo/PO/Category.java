package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Category
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/26 13:47
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Category {
    private int categoryId;
    private String categoryName;
    private int father;
}
