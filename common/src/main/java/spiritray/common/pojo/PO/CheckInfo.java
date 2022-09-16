package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:CheckInfo
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/14 0:24
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CheckInfo {
    private String commodityId;
    private Timestamp applyDate;
    private Timestamp checkDate;
    private long staffId;
    private int checkCode;
    private String remark;
}
