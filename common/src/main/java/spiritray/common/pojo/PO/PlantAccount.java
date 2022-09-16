package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:PlantAccount
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/19 17:25
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PlantAccount {
    private int paId;
    private int accaId;
    private String appId;
    private String accountNo;
    private String accountKey;
    private int isUseable;
}
