package spiritray.common.tool;

import spiritray.common.pojo.DTO.RpsMsg;

/**
 * ClassName:CheckResponseInf
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/4/21 16:40
 * @Author:灵@email
 */
public class CheckResponseInfTool {
    /*检测操作是否大于0并返回普通操作信息*/
    public static RpsMsg checkCodeAndReturnOften(int code) {
        if (code > 0) {
            return new RpsMsg().setStausCode(200).setMsg("修改成功");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("修改失败");
        }
    }
}
