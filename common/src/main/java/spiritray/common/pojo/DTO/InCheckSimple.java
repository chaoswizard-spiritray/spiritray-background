package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:InCheckSimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 12:03
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InCheckSimple {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private String checkInfo;//审核信息
    private int checkState;//审核状态码
}
