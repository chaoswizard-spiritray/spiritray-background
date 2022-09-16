package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:SellerAccount
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/5/12 12:02
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SellerAccount {
    private int accountId;//账户编号
    private String storeId;//店铺id
    private String accountNo;//商户号
    private String accountName;//账户所有者
    private int accaId;//账户类别
    private int isCollections;//是否用于收款
}
