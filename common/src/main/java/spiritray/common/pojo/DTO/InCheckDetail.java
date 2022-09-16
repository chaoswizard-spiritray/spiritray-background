package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:InCheckDetail
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/12 16:30
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InCheckDetail {
    private String commodityId;//商品id
    private String masterMap;//商品主图
    private String commodityName;//商品名称
    private String checkInfo;//审核信息
    private int checkState;//审核状态码
    private Timestamp applyDate;//发起时间
    private Timestamp checkDate;//审核时间
    private long staffId;//审核工号
    private String remark;//审核备注
}
