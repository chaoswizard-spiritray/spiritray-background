package spiritray.common.interceptor;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import spiritray.common.config.InterceptorConfig;
import spiritray.common.pojo.BO.ExcludeUriAndMethod;
import spiritray.common.pojo.DTO.RpsMsg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ClassName:StoreInterceptor
 * Package:spiritray.common.interceptor
 * Description:
 *
 * @Date:2022/4/21 16:21
 * @Author:灵@email
 */
public class StoreInterceptor implements HandlerInterceptor {
    @Autowired
    @Qualifier("storeEUAM")
    private ExcludeUriAndMethod excludeUriAndMethod;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        } else {
            //从头部中获取店铺id以及状态信息
            String storeId = request.getHeader("storeId");
            HttpSession session = request.getSession();
            String storeIdS = (String) session.getAttribute("storeId");
            //  Integer status = (Integer) session.getAttribute("storeStatus");
            if ((storeId != null && storeId.equals(storeIdS))) {
                return true;
            } else {
                response.getWriter().write(JSONUtil.toJsonStr(new RpsMsg().setStausCode(300).setMsg("无权限")));
                return false;
            }
        }
    }
}
