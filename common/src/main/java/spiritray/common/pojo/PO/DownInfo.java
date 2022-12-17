package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:DownInfo
 * Package:spiritray.common.pojo.PO
 * Description:
 * 商品下架信息
 *
 * @Date:2022/12/13 20:53
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DownInfo {
    private String commodityId;
    private Timestamp downDate;
    private String downDes;
    private Long staff;
}
