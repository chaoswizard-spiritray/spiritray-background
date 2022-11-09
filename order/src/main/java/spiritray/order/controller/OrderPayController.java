package spiritray.order.controller;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.BO.AliAppPayParam;
import spiritray.common.pojo.BO.AppPayRps;
import spiritray.common.pojo.BO.BackPayParam;
import spiritray.common.pojo.BO.WechatAppPayParam;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Cpi;
import spiritray.common.pojo.PO.PlantAccount;
import spiritray.order.mapper.CpiMapper;
import spiritray.order.mapper.OrderDetailMapper;
import spiritray.order.mapper.OrderMapper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ClassName:OrderPayController
 * Package:spiritray.order.controller
 * Description:
 *
 * @Date:2022/10/18 10:53
 * @Author:灵@email
 */
@RestController
@RequestMapping("/order/pay")
public class OrderPayController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CpiMapper cpiMapper;

    private final String PLANT_URL = "http://localhost:8083";

    private final String ORDER_URL = "http://localhost:8082";

    private final String BACK_NOTIFY_URL = ORDER_URL + "/pay/callback/back";//退款回调地址

    private String KEY_NAME = "accountKey";//头部信息中变量名称

    private String KEY = "ACCOUNT-CHECK-KEY-SHA256-1";//密钥

    private String ALGORITHM = DigestAlgorithm.SHA256.getValue();//加密算法

    /*总订单支付*/
    @GetMapping("/together/{payCate}/{orderId}")
    public RpsMsg getOrderPayTogether(@PathVariable int payCate, @PathVariable String orderId) {
        //验证订单是否已经付款
        if (orderDetailMapper.selectOrderDetailPaidByOrderNumber(orderId).size() > 0) {
            return new RpsMsg().setStausCode(300).setMsg("订单已付款");
        }
        //验证支付方式合法性
        RpsMsg rpsMsg = null;
        try {
            rpsMsg = restTemplate.getForObject(new URI(PLANT_URL + "/plant/account/cate/verify/" + payCate), RpsMsg.class);
        } catch (URISyntaxException e) {
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
        }
        //如果支付类型存在
        if (rpsMsg != null && rpsMsg.getStausCode() == 200 && (Boolean) rpsMsg.getData()) {
            //获取订单总金额
            float amount = 0;
            try {
                amount = orderMapper.selectOrderAllAmount(orderId);
            } catch (Exception e) {
                return new RpsMsg().setStausCode(300).setMsg("订单不存在");
            }
            //获取平台收款账号信息
            PlantAccount account = getPlantAccount(payCate);
            if (account == null) {
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
            }
            //拉取支付数据并返回到前端
            AppPayRps appPayRps = getPayData(payCate, orderId, amount, account);
            if (appPayRps.getData() != null) {
                return new RpsMsg().setStausCode(200).setData(appPayRps.getData());
            } else {
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
            }
        } else {
            return new RpsMsg().setStausCode(200).setMsg("支付方式不合法");
        }
    }

    /*订单细节单独支付*/
    @GetMapping("/detail/{payCate}/{orderId}/{ooId}")
    public RpsMsg getOrderPayDetail(@PathVariable int payCate, @PathVariable String orderId, @PathVariable int ooId) {
        //验证订单是否已经付款
        int state = orderDetailMapper.selectDetailStateById(orderId, ooId);
        if (state > 0) {
            return new RpsMsg().setStausCode(300).setMsg("订单已付款");
        }
        //验证支付方式合法性
        RpsMsg rpsMsg = null;
        try {
            rpsMsg = restTemplate.getForObject(new URI(PLANT_URL + "/plant/account/cate/verify/" + payCate), RpsMsg.class);
        } catch (URISyntaxException e) {
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
        }
        //如果支付类型存在
        if (rpsMsg != null && rpsMsg.getStausCode() == 200 && (Boolean) rpsMsg.getData()) {
            //获取订单细节金额
            Float amount = orderDetailMapper.selectDetailTotalAmountById(orderId, ooId);
            if (amount == null) {
                return new RpsMsg().setStausCode(300).setMsg("订单已付款");
            }
            //获取平台收款账号信息
            PlantAccount account = getPlantAccount(payCate);
            if (account == null) {
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
            }
            //拉取支付数据并返回到前端
            AppPayRps appPayRps = getPayData(payCate, orderId + ooId, amount, account);
            if (appPayRps.getData() != null) {
                return new RpsMsg().setStausCode(200).setData(appPayRps.getData());
            } else {
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
            }
        } else {
            return new RpsMsg().setStausCode(200).setMsg("支付方式不合法");
        }
    }

    /*未发货订单细节取消退款*/
    @PostMapping("/detail/back")
    public RpsMsg postBackOrderDetailMoney(String orderNumber, int odId) {
        //先获取订单细节付款信息
        Cpi cpi = cpiMapper.selectCpiByCpiId(orderNumber + odId);
        //判断付款方式获取支付商户号密钥
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(KEY_NAME, DigestUtil.digester(ALGORITHM).digestHex(KEY));
        HttpEntity httpEntity = new HttpEntity(new LinkedMultiValueMap<>(), httpHeaders);
        //请求获取指定类型的退款商户
        ResponseEntity<RpsMsg> resEntity = restTemplate.exchange(PLANT_URL + "/plant/account/" + cpi.getAccaId() + "/" + cpi.getPlantAccount(), HttpMethod.GET, httpEntity, RpsMsg.class);
        RpsMsg rpsMsg = resEntity.getBody();
        if ((!resEntity.getStatusCode().is2xxSuccessful()) || (rpsMsg.getData() == null)) {
            //如果是数据为空表明系统无可用正常的商户我们需要消息通知平台员工该订单退款商户存在问题
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙，稍后再试");
        }
        //如果获取到商户信息，创建退款请求参数
        PlantAccount plantAccount = JSONUtil.toBean(JSONUtil.toJsonStr(rpsMsg.getData()), PlantAccount.class);
        //如果该账户没有被使用
        BackPayParam backPayParam = new BackPayParam()
                .setMchId(plantAccount.getAccountNo())
                .setMoney(cpi.getPayMoney())
                .setNotifyUrl(BACK_NOTIFY_URL)
                .setOutTradeNo(cpi.getPayNo())
                .setOutTradeRefundNo(cpi.getCpiId())
                .setRefundDesc("商品订单:" + orderNumber + odId + "退款")
                .setSign(plantAccount.getAccountKey());
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("param", backPayParam);
        multiValueMap.add("accaId", plantAccount.getAccaId());
        httpEntity = new HttpEntity(multiValueMap, new HttpHeaders());
        ResponseEntity<AppPayRps> responseEntity = restTemplate.exchange(ORDER_URL + "/pay/back", HttpMethod.PUT, httpEntity, AppPayRps.class);
        //如果访问出错
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙，稍后再试");
        } else {
            AppPayRps appPayRps = (AppPayRps) responseEntity.getBody();
            //如果返回的状态失败，同样返回
            if (appPayRps.getCode() == 0) {
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙，稍后再试");
            } else {
                return new RpsMsg().setStausCode(200).setMsg("退款成功");
            }
        }
    }

    /*请求平台收款账户*/
    private PlantAccount getPlantAccount(int cate) {
        //创建请求实体对象
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(KEY_NAME, DigestUtil.digester(ALGORITHM).digestHex(KEY));//添加密钥
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        RpsMsg rpsMsg = restTemplate.exchange(PLANT_URL + "/plant/account/useable/" + cate + "/first", HttpMethod.GET, httpEntity, RpsMsg.class).getBody();
        if (rpsMsg != null && rpsMsg.getStausCode() == 200 && rpsMsg.getData() != null) {
            //因为resttemplate返回的是一个map,通过json中间转换为我们需要的类型
            return JSONUtil.toBean(JSONUtil.parseObj(rpsMsg.getData()), PlantAccount.class);
        } else {
            return null;
        }
    }

    /*根据支付方式拉取支付数据*/
    private AppPayRps getPayData(int payCate, String orderId, float amount, PlantAccount collectionAccount) {
        //封装付款信息
        AppPayRps payData;
        if (payCate == 1) {
            AliAppPayParam aliAppPayParam = new AliAppPayParam();
            aliAppPayParam.setAttach(JSON.toJSONString(new SSMap(orderId, amount + "")))
                    .setBody(JSON.toJSONString(new SSMap(orderId, amount + "")))
                    .setMch_id(collectionAccount.getAccountNo())
                    .setNotify_url("http://localhost:8082/pay/callback/app")
                    .setOut_trade_no(orderId)
                    .setSign(collectionAccount.getAccountKey())
                    .setTotal_fee(amount + "");
            payData = getThridPayData(1, JSON.toJSONString(aliAppPayParam));
        } else {
            WechatAppPayParam wechatAppPayParam = new WechatAppPayParam();
            wechatAppPayParam.setApp_id(collectionAccount.getAppId())
                    .setBody(JSON.toJSONString(new SSMap(orderId, amount + "")))
                    .setMch_id(collectionAccount.getAccountNo())
                    .setNotify_url("http://localhost:8082/pay/callback/app")
                    .setOut_trade_no(orderId)
                    .setSign(collectionAccount.getAccountKey())
                    .setTotal_fee(amount + "");
            payData = getThridPayData(2, JSON.toJSONString(wechatAppPayParam));
        }
        return payData;
    }

    /*发送请求获取支付数据*/
    private AppPayRps getThridPayData(int cate, String param) {
        //创建请求实体对象，必须要创建头部,且头部在后，参数映射在前，不然请求会调用失败
        MultiValueMap valueMap = new LinkedMultiValueMap();
        valueMap.add("param", param);
        HttpEntity httpEntity = new HttpEntity(valueMap, new HttpHeaders());
        //请求数据
        switch (cate) {
            case 1: {
                return restTemplate.exchange(ORDER_URL + "/pay/app/data/ali", HttpMethod.PUT, httpEntity, AppPayRps.class).getBody();
            }
            case 2: {
                return restTemplate.exchange(ORDER_URL + "/pay/app/data/wechat", HttpMethod.PUT, httpEntity, AppPayRps.class).getBody();
            }
            default: {
                return null;
            }
        }
    }

}

