package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import spiritray.common.pojo.PO.Cav;

import java.util.List;

/**
 * ClassName:CartCommodity
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 17:16
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CartCommodity {
    private long cartId;
    private String commodityId;
    private String commodityName;
    private boolean isUseable;//当前购物车商品是否有效
    private String storeHead;
    private String storeName;
    private String masterMap;
    private List<Cav> cavs;
    private int commodityNum;
}
