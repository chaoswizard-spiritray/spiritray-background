package spiritray.consumer.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import javafx.geometry.Pos;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.BO.MsgCode;
import spiritray.common.pojo.DTO.LSS;
import spiritray.common.pojo.DTO.RegisterDTO;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.Consumer;
import spiritray.common.tool.CodeTool;
import spiritray.common.tool.EmailSendTool;
import spiritray.consumer.mapper.ConsumerMapper;
import spiritray.consumer.service.ConsumerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * ClassName:ConsumerController
 * Package:spriritray.consumer.controller
 * Description:
 * 买家个人信息相关接口
 *
 * @Date:2022/3/30 3:36
 * @Author:灵@email
 */
@RestController
@RequestMapping("/consumer")
public class ConsumerController {
    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ConsumerMapper consumerMapper;

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sendFrom;//将配置的发送方映射

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private HttpHeaders httpHeaders;


    /*注册*/
    @PostMapping(value = "/register")

    public RpsMsg register(@RequestBody RegisterDTO registerDTO, HttpServletRequest request, HttpSession session) {
        //获取验证码
        session.removeAttribute("phone");
        session.removeAttribute("storeId");
        MsgCode backCode = (MsgCode) request.getSession().getAttribute("code");
        //获取传来的验证码
        String code = (String) registerDTO.getCode();
        //获取注册对象
        Consumer consumer = (Consumer) registerDTO.getConsumer();
        //设置默认图片
        consumer.setConsumerHead("/static/consumer/head/default.png");
        //调用注册逻辑
        return consumerService.register(backCode, code, consumer);
    }

    /*登录*/
    @RequestMapping(value = "/login/{phone}/{password}/{code}", method = RequestMethod.GET)
    public RpsMsg login(@PathVariable long phone, @PathVariable String password, @PathVariable String code, HttpSession session) {
        session.removeAttribute("phone");
        session.removeAttribute("storeId");
        Consumer consumer = new Consumer();
        consumer.setConsumerPhone(phone);
        consumer.setConsumerPassword(password);
        return consumerService.login((MsgCode) session.getAttribute("code"), code, consumer);
    }

    /*查询当前登录的买家个人信息*/
    @GetMapping("/info")
    public RpsMsg getConsumerInf(HttpSession session) {
        return consumerService.queryConsumerByConsumer(new Consumer().setConsumerPhone((Long) session.getAttribute("phone")));
    }

    /*修改个人昵称*/
    @RequestMapping(value = "/info/nickname", method = RequestMethod.PUT)
    public RpsMsg modifyNickName(String nickname, HttpSession session) {
        return consumerService.modifyConsumer(new Consumer().setConsumerPhone((Long) session.getAttribute("phone")).setConsumerNickname(nickname));
    }

    /*修改性别*/
    @PutMapping("/info/sex")
    public RpsMsg modifySex(int sex, HttpSession session) {
        return consumerService.modifyConsumer(new Consumer().setConsumerPhone((Long) session.getAttribute("phone")).setConsumerSex((byte) sex));
    }

    /*修改个人头像*/
    @SneakyThrows
    @RequestMapping(value = "/info/head", method = RequestMethod.PUT)
    public RpsMsg modifyHead(MultipartFile file, String imgPath, HttpSession session) {
        if (imgPath.indexOf("default") < 0) {
            //如果头像不是默认头像，就先删除
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            map.add("path", imgPath);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(map, httpHeaders);
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, httpEntity, Boolean.class);
            if (!responseEntity.getBody()) {
                //如果删除失败直接返回失败信息
                return new RpsMsg().setMsg("修改失败").setStausCode(300);
            }
        }
        //上传新的头像
        String url = "/static/consumer/head";
        String fileName = String.valueOf(UUID.randomUUID()) + RandomUtil.randomString(2);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
        map.add("path", url);
        map.add("file", commonInputStreamResource);
        map.add("fileName", fileName);
        HttpEntity httpEntity = new HttpEntity(map, httpHeaders);
        String newUrl = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class).getBody();
        if (newUrl == null) {
            return new RpsMsg().setMsg("修改失败").setStausCode(300);
        }
        //创建一个consumer然后修改信息
        return consumerService.modifyConsumer(new Consumer().setConsumerPhone((Long) session.getAttribute("phone")).setConsumerHead(newUrl));
    }

    /*修改密码*/
    @PutMapping("/info/password")
    public RpsMsg modifyPassword(String password, HttpSession session) {
        return consumerService.modifyConsumer(new Consumer().setConsumerPhone((Long) session.getAttribute("phone")).setConsumerPassword(new String(MD5.create().digest(password))));
    }

    /*找回密码*/
    @PostMapping("/info/backpassword")
    public RpsMsg backPassward(String params) {
        JSONObject jsonObject = JSON.parseObject(params);
        System.out.println(redisTemplate.opsForHash().entries("emailCodes"));
        System.out.println(jsonObject.get("email"));
        MsgCode realCode = (MsgCode) redisTemplate.opsForHash().get("emailCodes", jsonObject.get("email"));
        if (realCode == null || (!CodeTool.isLive(realCode, String.valueOf(jsonObject.get("code")))) || (!realCode.equals(jsonObject.get("code")))) {
            redisTemplate.opsForHash().delete("emailCodes", jsonObject.get("email"));
            return new RpsMsg().setStausCode(300).setMsg("设置失败，验证码无效");
        }
        redisTemplate.opsForHash().delete("emailCodes", jsonObject.get("email"));
        return consumerService.modifyConsumer(new Consumer().setConsumerPhone((Long) jsonObject.get("phone")).setConsumerPassword(new String(MD5.create().digest(String.valueOf(jsonObject.get("email"))))));
    }

    /*查询收货地址*/
    @RequestMapping(value = "/info/addresses", method = RequestMethod.GET)
    public RpsMsg getAddresses(HttpSession session) {
        return consumerService.queryAddress((Long) session.getAttribute("phone"));
    }

    /*修改收货地址*/
    @RequestMapping(value = "/info/addresses", method = RequestMethod.PUT)
    public RpsMsg modifyAddresses(String address, HttpSession session) {
        Address address1 = JSONObject.parseObject(address, Address.class);
        address1.setConsumerPhone((Long) session.getAttribute("phone"));
        return consumerService.modifyAddressById(address1);
    }

    /*添加收货地址*/
    @RequestMapping(value = "/info/addresses", method = RequestMethod.POST)
    public RpsMsg addAddresses(String location, HttpSession session) {
        Address address = JSONObject.parseObject(location, Address.class);
        address.setConsumerPhone((Long) session.getAttribute("phone")).setAddressId(String.valueOf(UUID.randomUUID()));
        return consumerService.addAddress(address);
    }

    /*删除收货地址*/
    @DeleteMapping("/info/addresses")
    public RpsMsg deleteAddress(String addressId, HttpSession session) {
        return consumerService.removeAddress(addressId, (Long) session.getAttribute("phone"));
    }


    /*请求普通验证码*/
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public Map messageCode(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        //生成随机验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(60, 30, 4, 20);
        //封装为验证码信息存储在session中,有效时间60秒
        session.setAttribute("code", new MsgCode(lineCaptcha.getCode(), System.currentTimeMillis(), 120000));
        HashMap map = new HashMap();
        map.put("data", "data:image/" + "jpeg" + ";base64," + lineCaptcha.getImageBase64());
        return map;
    }

    /*发送电子邮件验证码*/
    @GetMapping("/emailCode/{phone}")
    public RpsMsg emailCode(@PathVariable Long phone) {
        //获取邮箱
        String email = consumerMapper.selectEmailByPhone(phone);
        if (email == null) {
            return new RpsMsg().setMsg("未绑定邮箱").setStausCode(300);
        }
        MsgCode msgCode = new MsgCode(RandomUtil.randomString(4), System.currentTimeMillis(), 60000);
        redisTemplate.opsForHash().put("emailCodes", email, msgCode);
        javaMailSender.send(EmailSendTool.getSimpleMailMessage(sendFrom, email, "spiritray密码重置", "本次验证码为" + msgCode.getCode() + "有效时间为120秒"));
        return new RpsMsg().setMsg("邮件发送成功，及时查收").setStausCode(200).setData(email);
    }

    /*批量获取买家昵称和头像*/
    @GetMapping("/headAndName/many")
    public RpsMsg getHeadAndNameMany(@RequestParam("consumerPhones") String consumerPhones) {
        List<Long> phones = JSON.parseArray(consumerPhones, Long.class);
        if (phones == null || phones.size() == 0) {
            return new RpsMsg().setStausCode(200).setMsg("查询成功");
        }
        List<Consumer> consumers = consumerMapper.selectNameAndHeadByPhone(phones);
        List<LSS> result = new ArrayList<>();
        for (Long phone : phones) {
            for (Consumer consumer : consumers) {
                if (phone.longValue() == consumer.getConsumerPhone()) {
                    result.add(new LSS(phone, consumer.getConsumerNickname(), consumer.getConsumerHead()));
                }
            }
        }
        return new RpsMsg().setData(result).setStausCode(200).setMsg("查询成功");
    }

    /*获取指定买家头像与昵称*/
    @GetMapping("/headAndName/simple/{phone}")
    public RpsMsg getHeadAndNickNameByPhone(@PathVariable long phone) {
        if (phone <= 0) {
            return new RpsMsg().setStausCode(200).setMsg("查询成功");
        }
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(consumerMapper.selectConsumerByConsumer(new Consumer().setConsumerPhone(phone)));
    }
}
