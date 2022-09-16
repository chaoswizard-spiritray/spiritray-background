package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:HomeCommoditySimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/15 10:43
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HomeCommoditySimple {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private float favorableRate;//好评率
    private float priceMin;//价格最小值
    private float priceMax;//价格最大值
}
