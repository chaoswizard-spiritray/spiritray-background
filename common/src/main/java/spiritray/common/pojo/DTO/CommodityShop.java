package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:CommodityShop
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/15 20:48
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommodityShop {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private String storeId;//店铺id
    private float shipping;//运费
    private String address;//发货地址
    private float priceMin;//最低价格
}
