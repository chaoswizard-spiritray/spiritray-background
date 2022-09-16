package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Cart
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/27 17:26
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Cart {
    private long cartId;
    private String commodityId;
    private long consumerPhone;
    private String commodityName;
    private int commodityNum;
    private String skuValue;
    private String skuMap;
    private float totalFee;
}
