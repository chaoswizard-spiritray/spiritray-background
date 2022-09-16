package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Commodity
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/26 21:18
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Commodity {
    private String commodityId;
    private String storeId;
    private int categoryId;
    private float shipping;//运费
    private String commodityName;
    private String masterMap;
    private String commodityDescribe;
    private String address;
}
