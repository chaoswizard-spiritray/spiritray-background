package spiritray.consumer.service.imp;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.BO.CommonInf;
import spiritray.common.pojo.BO.MsgCode;
import spiritray.common.pojo.DTO.LoginDTO;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.Consumer;
import spiritray.common.tool.AlgorithmTool;
import spiritray.common.tool.CodeTool;
import spiritray.consumer.mapper.ConsumerMapper;
import spiritray.consumer.service.ConsumerService;

import java.net.URI;

/**
 * ClassName:ConsumerServiceImp
 * Package:spriritray.consumer.service.imp
 * Description:
 *
 * @Date:2022/4/14 8:45
 * @Author:灵@email
 */
@Slf4j
@Service
public class ConsumerServiceImp implements ConsumerService {
    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier("commonInf")
    private CommonInf commonInf;

    @SneakyThrows
    @Override
    public RpsMsg register(MsgCode backCode, String code, Consumer consumer) {
        RpsMsg rpsMsg = new RpsMsg();//返回信息对象
        //是否放行
        boolean isAccess = CodeTool.isLive(backCode, code);
        //如果有效就先调用查询用户信息服务，查询该账号是否存在
        if (isAccess) {
            Consumer consumer1 = new Consumer();
            consumer1.setConsumerPhone(consumer.getConsumerPhone());
            //如果账号存在，就不通过注册
            if (consumerMapper.selectConsumerByConsumer(consumer1) != null) {
                return rpsMsg.setStausCode(300).setMsg("该账号已经存在");
            } else {
                //将密码加密
                consumer.setConsumerPassword(String.valueOf(new String(MD5.create().digest(consumer.getConsumerPassword()))));
                //保存数据
                if (consumerMapper.insertConsumer(consumer) <= 0) {
                    return rpsMsg.setStausCode(300).setMsg("注册失败，稍后再试");
                } else {
                    return rpsMsg.setStausCode(200).setMsg("注册成功");
                }
            }
        } else {
            //如果验证码已经过期就直接返回，建议将验证码验证放在拦截器中验证
            return rpsMsg.setStausCode(300).setMsg("验证码无效");
        }
    }

    @SneakyThrows
    @Override
    public RpsMsg login(MsgCode backCode, String code, Consumer consumer) {
        RpsMsg rpsMsg = new RpsMsg();
        boolean isLive = CodeTool.isLive(backCode, code);
        Consumer consumer1 = null;
        //如果还存活就查询
        if (isLive) {
            consumer.setConsumerPassword(new String(MD5.create().digest(consumer.getConsumerPassword())));
            consumer1 = consumerMapper.selectConsumerByConsumer(consumer);
            if (consumer1 != null) {
                RpsMsg enterMsg = null;
                //查询该用户是否入驻
                try {
                    enterMsg = restTemplate.getForObject(URI.create(commonInf.getSellerSeviceUrl() + "/seller/enter/" + consumer.getConsumerPhone()), RpsMsg.class);
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.error(String.valueOf(e.getCause()));
                } finally {
                    if (enterMsg != null && enterMsg.getStausCode() == 200) {
                        //如果状态码为200说明已入驻
                        consumer1.setIsEnter((byte) 1);
                    }
                    //更新密钥
                    String key = RandomUtil.randomString(5);
                    redisTemplate.opsForHash().put("jwtKeys", consumer.getConsumerPhone() + "", key);
                    //生成JWT
                    //从获取加密算法名称
                    String methodName = (String) redisTemplate.opsForHash().get("algorithmName", "loginAlgorithmName");
                    Algorithm algorithm = AlgorithmTool.getJwtAlgorithm(methodName, key);
                    //创建jwt并返回
                    String jwt = JWT.create()
                            .withClaim("phone", consumer.getConsumerPhone())
                            .withExpiresAt(DateUtil.nextMonth())
                            .sign(algorithm);
                    LoginDTO loginDTO = new LoginDTO(consumer1, jwt);
                    return rpsMsg.setStausCode(200).setMsg("登录成功").setData(loginDTO);
                }
            } else {
                return rpsMsg.setStausCode(300).setMsg("账号或密码有误");
            }
        } else {
            return rpsMsg.setStausCode(300).setMsg("验证码无效");
        }
    }

    @Override
    public RpsMsg queryConsumerByConsumer(Consumer consumer) {
        consumer = consumerMapper.selectConsumerByConsumer(consumer);
        if (consumer == null) {
            return new RpsMsg().setStausCode(300).setMsg("查询失败");
        }
        RpsMsg enterMsg = restTemplate.getForObject(URI.create(commonInf.getSellerSeviceUrl() + "/seller/enter/" + consumer.getConsumerPhone()), RpsMsg.class);
        if (enterMsg != null && enterMsg.getStausCode() == 200) {
            //如果状态码为200说明已入驻
            consumer.setIsEnter((byte) 1);
        }
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(consumer);
    }

    @Override
    public RpsMsg modifyConsumer(Consumer consumer) {
        if (consumerMapper.updateConsumer(consumer) > 0) {
            return new RpsMsg().setStausCode(200).setMsg("修改成功").setData(consumer);
        }
        return new RpsMsg().setStausCode(300).setMsg("修改失败");
    }

    @Override
    public RpsMsg queryAddress(long phone) {
        return new RpsMsg().setStausCode(200).setData(consumerMapper.selectAddressByPhone(phone));
    }

    @Override
    public RpsMsg modifyAddressById(Address address) {
        if (consumerMapper.updateAddressById(address) >= 0) {
            return new RpsMsg().setStausCode(200).setMsg("修改成功");
        }
        return new RpsMsg().setStausCode(300).setMsg("修改失败");
    }

    @Override
    public RpsMsg addAddress(Address address) {
        if (consumerMapper.insertAddress(address) >= 0) {
            return new RpsMsg().setStausCode(200).setMsg("添加成功").setData(address);
        }
        return new RpsMsg().setStausCode(300).setMsg("添加失败");
    }

    @Override
    public RpsMsg removeAddress(String addressId, long phone) {
        if (consumerMapper.deleteAddressById(addressId, phone) > 0) {
            return new RpsMsg().setStausCode(200).setMsg("删除成功");
        } else {
            return new RpsMsg().setMsg("删除失败").setStausCode(300);
        }
    }
}
