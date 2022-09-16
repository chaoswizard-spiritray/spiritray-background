package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Order
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/18 19:58
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Order {
    private String orderNumber;
    private long consumerPhone;
    private Timestamp orderDate;
    private float totalAmount;
}
