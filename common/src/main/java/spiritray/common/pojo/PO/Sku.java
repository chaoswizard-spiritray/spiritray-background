package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Sku
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/5/24 8:25
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Sku {
    private String commodityId;
    private String skuValue;
    private String skuMap;
    private float skuPrice;
    private int num;
}
