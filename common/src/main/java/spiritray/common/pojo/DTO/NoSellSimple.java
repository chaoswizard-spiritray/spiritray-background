package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ClassName:NoSellSimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 12:15
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class NoSellSimple {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private Date downDate;//下架的日期
    private String downCommotion;//下架备注
}
