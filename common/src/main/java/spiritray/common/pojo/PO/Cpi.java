package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Cpi
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/19 14:49
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Cpi {
    private String cpiId;
    private String payNo;
    private int accaId;
    private String plantAccount;
    private float payMoney;
    private Timestamp payDate;
}
