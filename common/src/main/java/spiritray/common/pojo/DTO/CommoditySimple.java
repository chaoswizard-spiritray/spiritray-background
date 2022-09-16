package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName:CommoditySimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/13 16:54
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommoditySimple {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private String storeId;//店铺id

}
