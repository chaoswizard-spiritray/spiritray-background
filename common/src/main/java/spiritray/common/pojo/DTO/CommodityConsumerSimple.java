package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:CommodityConsumerSimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 21:07
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommodityConsumerSimple {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private String commodityDescribe;//商品描述
    private float price;//某一个价格
}
