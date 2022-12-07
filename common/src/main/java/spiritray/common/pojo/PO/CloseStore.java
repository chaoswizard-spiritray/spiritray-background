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
    private String storeId;
    private Timestamp applyDate;
    private String applyCause;
    private Long closeDay;
    private Integer checkCode;
    private Long checkStaff;
    private Timestamp paseDate;
    private Integer isDelete;
}
