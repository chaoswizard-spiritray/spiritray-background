package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:CommodityComment
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/11/11 10:13
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommodityComment {
    private String commentNo;
    private String commentContent;
    private String attchedMap;
    private Integer starLevel;
    private Timestamp startDate;
    private String consumerHead;//头像
    private String consumerNickname;//昵称
    private String skuValue;//评论购买商品的sku值
}
