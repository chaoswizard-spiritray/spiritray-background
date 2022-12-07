package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Slide
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/12/2 14:35
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Slide {
    private String slideNo;
    private String storeId;
    private Timestamp getDate;
    private int isDelete;
}
