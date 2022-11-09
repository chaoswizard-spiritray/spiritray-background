package spiritray.common.interceptor;

import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Base64;
import java.util.Map;

/**
 * ClassName:SessionKeepInterceptor
 * Package:spiritray.common.interceptor
 * Description:
 *
 * @Date:2022/6/16 19:21
 * @Author:灵@email
 */
public class SessionKeepInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String jwt = request.getHeader("jwt");
        String storeId = request.getHeader("storeId");
        String staffId = request.getHeader("staffId");
        HttpSession session = request.getSession();
        if (jwt != null) {
            jwt = jwt.replaceAll("[\"]", "");//去除"
            //解析负载
            Map<String, Object> map = (Map<String, Object>) JSON.parse(new String(Base64.getDecoder().decode(JWT.decode(jwt).getPayload())));
            Long phone = (Long) map.get("phone");
            if (phone != null) {
                //说明是商家
                session.setAttribute("phone", phone);
            }
        }
        if (storeId != null) {
            session.setAttribute("storeId", storeId);
        }
        if (staffId != null) {
            session.setAttribute("staffId", staffId);
        }
        //反之说明没有登录
        return true;
    }
}
