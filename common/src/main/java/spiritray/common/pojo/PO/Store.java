package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ClassName:Store
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/21 15:19
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Store {
    private String storeId;
    private String sellerId;
    private String storeName;
    private String storeHead;
    private int status;
    private Date closeDate;
    private String closeCause;
    private int  closeDay;
}
