package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Pts
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/11/24 14:27
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Pts {
    private String ptsId;
    private String payNo;
    private String payAccount;
    private String getAccount;
    private Float payMoney;
    private String desc;
    private Timestamp addTime;
    private Timestamp payTime;
    private String payType;
    private Integer payStatus;
    private String reason;

}
