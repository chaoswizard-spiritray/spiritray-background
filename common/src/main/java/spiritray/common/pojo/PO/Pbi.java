package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Pbi
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/6/19 14:51
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Pbi {
    private String pbiId;
    private String yungouNo;
    private String payNo;
    private int accaId;
    private String plantAccount;
    private String backMoney;
    private Timestamp backDate;
}
