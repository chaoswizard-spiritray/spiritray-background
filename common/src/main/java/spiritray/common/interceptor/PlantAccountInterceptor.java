package spiritray.common.interceptor;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import spiritray.common.pojo.DTO.RpsMsg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ClassName:PlantAccountInterceptor
 * Package:spiritray.common.interceptor
 * Description:
 *
 * @Date:2022/10/23 8:35
 * @Author:灵@email
 */
public class PlantAccountInterceptor implements HandlerInterceptor {

    private String KEY = "ACCOUNT-CHECK-KEY-SHA256-1";//密钥

    private String ALGORITHM = DigestAlgorithm.SHA256.getValue();//加密算法

    private String KEY_NAME = "accountKey";//头部信息中变量名称

    /*用于控制平台账户权限，检测是否*/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //预先请求放行
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        //获取头部信息
        String encryptKey = request.getHeader(KEY_NAME);
        //如果头部中没有信息
        if (encryptKey == null) {
            //查看是否有jwt
            if (request.getHeader("jwt") != null) {
                return true;
            } else {
                //否则代表无权限访问
                response.getWriter().write(JSONUtil.toJsonStr(new RpsMsg().setMsg("无权限访问").setStausCode(400)));
                return false;
            }
        } else {
            //有密钥就取出验证
            if (encryptKey.equals(DigestUtil.digester(ALGORITHM).digestHex(KEY))) {
                return true;
            } else {
                //无权限访问
                response.getWriter().write(JSONUtil.toJsonStr(new RpsMsg().setMsg("无权限访问").setStausCode(400)));
                return false;
            }
        }
    }
}
