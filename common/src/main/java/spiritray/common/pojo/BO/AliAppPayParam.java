package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:AliAppPayParam
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/6/19 10:31
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AliAppPayParam {
    private String out_trade_no;
    private String total_fee;
    private String mch_id;
    private String body;
    private String attach;
    private String notify_url;
    private String hb_fq;
    private String sign;
}
