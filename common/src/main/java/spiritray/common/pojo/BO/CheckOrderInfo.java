package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:CheckOrderInfo
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/6/18 20:58
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckOrderInfo {
    private String storeId;
    private String commodityId;
    private float shipping;
    private String skuValue;
    private String skuMap;
    private float skuPrice;
}
