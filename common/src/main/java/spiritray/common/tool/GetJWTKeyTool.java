package spiritray.common.tool;

import java.io.UnsupportedEncodingException;

/**
 * ClassName:GetJWTKey
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/4/22 17:23
 * @Author:灵@email
 */
public class GetJWTKeyTool {
    private static String key = "abcd";

    public static byte[] getKey() throws UnsupportedEncodingException {
        return key.getBytes("utf-8");
    }
}
