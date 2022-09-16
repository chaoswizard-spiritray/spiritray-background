package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:CommodityCondition
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 20:51
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommodityCondition {
    private int page;//当前要展示的页
    private int pageNum;//每页显示的数目
    private String keyWord;//搜索的关键字
    private String sortName;//排序名称
    private String sortRule;//排序规则，正序或者逆序
}
