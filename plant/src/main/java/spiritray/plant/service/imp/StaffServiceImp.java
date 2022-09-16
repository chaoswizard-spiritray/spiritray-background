package spiritray.plant.service.imp;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import spiritray.common.pojo.BO.MsgCode;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.tool.AlgorithmTool;
import spiritray.common.tool.CodeTool;
import spiritray.common.tool.EmailSendTool;
import spiritray.plant.mapper.StaffMapper;
import spiritray.plant.service.StaffService;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName:StaffServiceImp
 * Package:spiritray.plant.service
 * Description:
 *
 * @Date:2022/6/13 8:42
 * @Author:灵@email
 */
@Service
public class StaffServiceImp implements StaffService {
    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sendFrom;//将配置的发送方映射

    @Override
    public RpsMsg sendCodeByStaffId(long staffId) {
        String mail = staffMapper.selectMailByStaffId(staffId);
        if (mail != null) {
            //发送邮箱验证码
            MsgCode msgCode = new MsgCode(RandomUtil.randomNumbers(6), System.currentTimeMillis(), 60000);
            redisTemplate.opsForHash().put("staffCodes", staffId + "", msgCode);
            javaMailSender.send(EmailSendTool.getSimpleMailMessage(sendFrom, mail, "你正在登录spiritray", "本次验证码为" + msgCode.getCode() + "有效时间为60秒"));
            return new RpsMsg().setStausCode(200).setMsg("邮箱验证码已发送");
        }
        return new RpsMsg().setStausCode(300).setMsg("工号不合法");
    }

    @Override
    public RpsMsg logon(long staffId, String code) {
        MsgCode msgCode = (MsgCode) redisTemplate.opsForHash().get("staffCodes", staffId + "");
        if (msgCode == null || (!CodeTool.isLive(msgCode, code))) {
            redisTemplate.opsForHash().delete("staffCodes", staffId + "");
            return new RpsMsg().setStausCode(300).setMsg("验证码无效");
        }
        redisTemplate.opsForHash().delete("staffCodes", staffId + "");
        //更新密钥
        String key = RandomUtil.randomString(5);
        redisTemplate.opsForHash().put("staffJwtKeys", staffId + "", key);
        //生成JWT
        //从获取加密算法名称
        String methodName = (String) redisTemplate.opsForHash().get("algorithmName", "loginAlgorithmName");
        Algorithm algorithm = AlgorithmTool.getJwtAlgorithm(methodName, key);
        //创建jwt并返回
        String jwt = JWT.create()
                .withClaim("staffId", staffId)
                .withExpiresAt(DateUtil.nextMonth())
                .sign(algorithm);
        Map<String, Object> map = new HashMap<>();
        map.put("staffId", staffId);
        map.put("jwt", jwt);
        return new RpsMsg().setMsg("登录成功").setStausCode(200).setData(map);
    }
}
