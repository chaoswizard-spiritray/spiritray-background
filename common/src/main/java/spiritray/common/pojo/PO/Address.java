package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:Address
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/4/27 16:26
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Address {
    private String addressId;
    private long consumerPhone;
    private String address;
    private String takeName;
    private long takePhone;
    private int isDefault;
}
