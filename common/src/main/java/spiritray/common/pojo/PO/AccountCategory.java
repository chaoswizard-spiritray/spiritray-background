package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:AccountCategory
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/5/24 19:06
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AccountCategory {
    public int accaId;
    public String accaName;
    public int isOpen;
}
