package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Attention
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/12/11 11:27
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Attention {
    private Long consumerPhone;
    private String storeId;
    private Timestamp startDate;
}
