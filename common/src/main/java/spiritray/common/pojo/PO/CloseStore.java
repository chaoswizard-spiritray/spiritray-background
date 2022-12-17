package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:CloseStore
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/12/1 22:31
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CloseStore {
    private String infoId;
    private Long closeStaff;
    private String storeId;
    private String sealCause;
    private Timestamp startDate;
    private Long closeDay;
    private Long overStaff;
    private String overInfo;
    private Timestamp overDate;
    private Integer isOver;
    private Integer isDelete;
}
