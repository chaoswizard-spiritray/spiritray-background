package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName:Seller
 * Package:spiritray.consumer.pojo.PO
 * Description:
 *
 * @Date:2022/4/17 16:10
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seller {
    private String sellerId;
    private String sellerName;
    private long sellerPhone;
    private String sellerPath;

}
