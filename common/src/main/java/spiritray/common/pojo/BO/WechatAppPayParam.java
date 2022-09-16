package spiritray.common.pojo.BO;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:AlipayParam
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/6/19 10:25
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class WechatAppPayParam {
    private String app_id;
    private String out_trade_no;
    private String total_fee;
    private String mch_id;
    private String body;
    private String attach;
    private String notify_url;
    private String auto;
    private String auto_node;
    private String config_no;
    private JSON biz_params;
    private String sign;
}
