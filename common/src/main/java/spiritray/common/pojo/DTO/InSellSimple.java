package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:InSellSimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 11:08
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InSellSimple {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private int inSellDay;//上架的天数
}
