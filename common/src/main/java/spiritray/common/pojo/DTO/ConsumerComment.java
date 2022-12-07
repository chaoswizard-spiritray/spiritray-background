package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import spiritray.common.pojo.PO.Comment;

/**
 * ClassName:ConsumerComment
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/12/1 11:16
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ConsumerComment {
    Comment comment;
    private String commodityName;
    private OrderDetailInfo orderDetailInfo;
}
