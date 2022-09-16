package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:History
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/19 20:46
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class History {
    private long consumerPhone;
    private String commodityId;
    private Timestamp startDate;
    private int lookTime;
}
