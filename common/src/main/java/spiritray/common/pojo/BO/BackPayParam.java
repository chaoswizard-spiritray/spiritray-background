package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:BackPayParam
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/11/6 13:59
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class BackPayParam {
    private String outTradeNo;
    private String mchId;
    private float money;
    private String outTradeRefundNo;
    private String refundDesc;
    private String notifyUrl;
    private String sign;
}
