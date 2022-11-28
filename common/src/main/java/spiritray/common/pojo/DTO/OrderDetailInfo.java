package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import spiritray.common.pojo.PO.Order;
import spiritray.common.pojo.PO.OrderDetail;

import java.sql.Timestamp;

/**
 * ClassName:OrderDetailInfo
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/11/24 10:18
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class OrderDetailInfo {
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
    private long consumerPhone;
    private Timestamp orderDate;
}
