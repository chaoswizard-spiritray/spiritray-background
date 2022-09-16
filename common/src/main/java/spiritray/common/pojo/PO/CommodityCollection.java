package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Collection
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/16 15:15
 * @Author:灵@email
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CommodityCollection {
    private long consumerPhone;
    private String commodityId;
    private Timestamp startDate;
}
