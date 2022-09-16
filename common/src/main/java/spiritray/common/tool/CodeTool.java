package spiritray.common.tool;

import spiritray.common.pojo.BO.MsgCode;

/**
 * ClassName:CodeTool
 * Package:spriritray.consumer.tool
 * Description:
 * 验证码工具包
 *
 * @Date:2022/4/14 15:11
 * @Author:灵@email
 */
public class CodeTool {
    static public boolean isLive(MsgCode backCode, String code) {
        //如果相同就比较是否过期，使用加法不要用减法。
        return (code.equals(backCode.getCode()))
                ?
                ((backCode.getStartT() + backCode.getEnableT()) > System.currentTimeMillis()
                        ?
                        true
                        :
                        false
                )
                :
                false;
    }
}
