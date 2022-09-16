package spiritray.common.interceptor;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.*;
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
 * ClassName:CodeInterceptor
 * Package:spriritray.consumer.interceptor
 * Description:
 *
 * @Date:2022/4/14 11:39
 * @Author:灵@email
 */
public class JWTInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        } else {
            request.setCharacterEncoding("utf-8");
            response.setCharacterEncoding("utf-8");
            //注意！！！！因为前端是直接将数据库中的jwt进行取出来然后进行传递，且缓存中存储的JSON字符串，所以带有",必须要去除掉，不然JWT验证失败
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
            long phone = (long) map.get("phone");
            String key = (String) redisTemplate.opsForHash().get("jwtKeys", phone + "");
            String methodName = (String) redisTemplate.opsForHash().get("algorithmName", "loginAlgorithmName");
            Algorithm algorithm = AlgorithmTool.getJwtAlgorithm(methodName, key);
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            try {
                jwtVerifier.verify(jwt);
                //如果JWT有效就将电话设置到session中
                HttpSession session = request.getSession();
                session.setAttribute("phone", phone);
                return true;
            } catch (Exception e) {
                rpsMsg.setStausCode(400).setMsg("无效身份");
                response.getWriter().write(JSONUtil.toJsonStr(rpsMsg));
                return false;
            }
        }
    }
}