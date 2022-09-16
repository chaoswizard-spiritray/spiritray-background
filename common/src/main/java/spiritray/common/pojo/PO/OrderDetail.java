package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:OrderDetail
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/18 20:00
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderDetail {
    private String orderNumber;
    private int odId;
    private String storeId;
    private String commodityId;
    private String skuValue;
    private String skuMap;
    private int commodityNum;
    private float totalAmount;
    private String addressMsg;
    private int state;
    private String logisticsNo;
    private Timestamp overDate;
}
