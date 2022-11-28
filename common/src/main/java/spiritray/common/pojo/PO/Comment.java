package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Comment
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/11/11 10:01
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Comment {
    private String commentNo;
    private String orderNumber;
    private Integer odId;
    private String commodityId;
    private Long consumerPhone;
    private String commentContent;
    private String attchedMap;
    private Integer starLevel;
    private Integer isAnonymous;
    private Timestamp startDate;
}
