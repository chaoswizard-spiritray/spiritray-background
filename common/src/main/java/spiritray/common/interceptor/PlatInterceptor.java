package spiritray.common.interceptor;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.tool.AlgorithmTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Base64;
import java.util.Map;

/**
 * ClassName:PlatInterceptor
 * Package:spiritray.common.interceptor
 * Description:
 *
 * @Date:2022/6/13 9:34
 * @Author:灵@email
 */
public class PlatInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        } else {
            String jwt = request.getHeader("jwt");
            RpsMsg rpsMsg = new RpsMsg();
            if (jwt == null) {
                rpsMsg.setMsg("请先登录").setStausCode(400);
                response.getWriter().write(JSONUtil.toJsonStr(rpsMsg));
                return false;
            }
            jwt = jwt.replaceAll("[\"]", "");//去除"
            //通过密钥和加密算法验证jwt
            Map<String, Object> map = (Map<String, Object>) JSON.parse(new String(Base64.getDecoder().decode(JWT.decode(jwt).getPayload())));
            long staffId = (long) map.get("staffId");
            String key = (String) redisTemplate.opsForHash().get("staffJwtKeys", staffId + "");
            String methodName = (String) redisTemplate.opsForHash().get("algorithmName", "loginAlgorithmName");
            Algorithm algorithm = AlgorithmTool.getJwtAlgorithm(methodName, key);
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            try {
                jwtVerifier.verify(jwt);
                HttpSession session = request.getSession();
                session.setAttribute("staffId", staffId);
                return true;
            } catch (Exception e) {
                rpsMsg.setStausCode(400).setMsg("无效身份");
                response.getWriter().write(JSONUtil.toJsonStr(rpsMsg));
                return false;
            }
        }
    }
}
