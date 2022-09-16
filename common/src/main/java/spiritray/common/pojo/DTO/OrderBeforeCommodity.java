package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import spiritray.common.pojo.PO.Sku;

/**
 * ClassName:OrderBeforeCommodity
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/18 20:08
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderBeforeCommodity {
    private String orderId;
    private String commodityId;
    private String commodityName;
    private Sku sku;
    private int commodityNum;
    private float shipping;
}
