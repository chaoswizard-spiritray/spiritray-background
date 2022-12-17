package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ClassName:NoSellDetail
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/12/14 10:39
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class NoSellDetail {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private Date downDate;//下架的日期
    private String downDes;//下架备注
    private Long staff;//下架员工
}
