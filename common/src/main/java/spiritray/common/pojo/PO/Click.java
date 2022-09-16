package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * ClassName:Click
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/19 21:31
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Click {
    private String clickNo;
    private String commodityId;
    private long clickNum;
    private Timestamp clickDate;
}
