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
import spiritray.common.pojo.PO.*;
import spiritray.common.tool.SystemMsgNotice;
import spiritray.order.mapper.CpiMapper;
import spiritray.order.mapper.OrderDetailMapper;
import spiritray.order.mapper.OrderMapper;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;

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

    private final String SELLER_URL = "http://localhost:8081";

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
                //通知平台账户问题
                Msg msg = new Msg("", 0L, 0L, 0, 0, "系统存在开启了支付方式却没有提供收款账户问题,导致买家支付失败,请立即处理", "text", 0, new Timestamp(new Date().getTime()), 0);
                SystemMsgNotice.notieMsg(restTemplate, msg);
                //返回买家端消息
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
                //通知平台账户问题
                Msg msg = new Msg("", 0L, 0L, 0, 0, "系统存在开启了支付方式却没有提供收款账户问题,导致买家支付失败,请立即处理", "text", 0, new Timestamp(new Date().getTime()), 0);
                SystemMsgNotice.notieMsg(restTemplate, msg);
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
    public RpsMsg postBackOrderDetailMoney(String orderNumber, int odId, HttpSession session) {
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
            Msg msg = new Msg("", 0L, 0L, 0, 0, "平台禁用了" + (cpi.getAccaId() == 1 ? "支付宝" : "微信") + "含有未完成订单的商户号:" + cpi.getPlantAccount(), "text", 0, new Timestamp(new Date().getTime()), 0);
            SystemMsgNotice.notieMsg(restTemplate, msg);
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

    /*已收货订单平台转账给商户*/
    @PostMapping("/detail/trans")
    public RpsMsg postTransOrderDetailMoney(String orderNumber, int odId) {
        //获取店铺id
        String storeId = orderDetailMapper.selectOrderDetailById(orderNumber, odId).getStoreId();
        //获取店铺电话
        Long phone = (Long) restTemplate.exchange(SELLER_URL + "/store/storeInf/phone/" + storeId, HttpMethod.GET, new HttpEntity<>(new LinkedMultiValueMap<>(), new HttpHeaders()), RpsMsg.class).getBody().getData();
        //先获取商家收款账户
        SellerAccount sellerAliAccount = getSellerCollectionAccount(1, storeId);
        SellerAccount sellerWchAccount = getSellerCollectionAccount(2, storeId);
        //获取平台可用转账账户
        PlantAccount plantAliAccount = getPlantAccount(1);
        PlantAccount plantWchAccount = getPlantAccount(2);
        PlantAccount transAccount = plantAliAccount;
        //确定商家收款账户
        SellerAccount collectionAccount = sellerAliAccount == null ? (sellerWchAccount == null ? null : sellerWchAccount) : sellerAliAccount;
        if (collectionAccount == null) {
            //通知商家添加账户
            Msg msg = new Msg("", 0L, phone, 0, 2, "订单:" + orderNumber + odId + "结束，平台转账失败，因为你没有可用收款账户，请及时添加并开启", "text", 0, new Timestamp(new Date().getTime()), 0);
            SystemMsgNotice.notieMsg(restTemplate, msg);
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
        } else {
            if (plantAliAccount == null && plantWchAccount == null) {
                //通知系统添加账户
                Msg msg = new Msg("", 0L, 0L, 0, 0, "订单:" + orderNumber + odId + "结束，平台转账失败，因为平台没有可用转账账户，请及时添加并开启", "text", 0, new Timestamp(new Date().getTime()), 0);
                SystemMsgNotice.notieMsg(restTemplate, msg);
                return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
            }
            if (collectionAccount == sellerAliAccount) {
                if (plantAliAccount == null && sellerWchAccount == null) {
                    //如果平台账户与商家账户错开
                    Msg msg = new Msg("", 0L, 0L, 0, 0, "订单:" + orderNumber + odId + "结束，平台转账失败，因为平台没有对应的类型可用转账账户，请及时添加并开启", "text", 0, new Timestamp(new Date().getTime()), 0);
                    SystemMsgNotice.notieMsg(restTemplate, msg);
                } else if (plantAliAccount == null && sellerWchAccount != null) {
                    //那么就使用微信账户
                    collectionAccount = sellerWchAccount;
                    transAccount = plantWchAccount;
                } else if (plantAliAccount != null) {
                    transAccount = plantAliAccount;
                }
            }
            if (collectionAccount == sellerWchAccount) {
                if (plantWchAccount == null && sellerAliAccount == null) {
                    collectionAccount.getAccaId();//无效行
                    //如果平台账户与商家账户错开
                    Msg msg = new Msg("", 0L, 0L, 0, 0, "订单:" + orderNumber + odId + "结束，平台转账失败，因为平台没有对应的类型可用转账账户，请及时添加并开启", "text", 0, new Timestamp(new Date().getTime()), 0);
                    SystemMsgNotice.notieMsg(restTemplate, msg);
                } else if (plantWchAccount == null && sellerAliAccount != null) {
                    //那么就使用微信账户
                    collectionAccount = sellerAliAccount;
                    transAccount = plantAliAccount;
                } else if (plantWchAccount != null) {
                    transAccount = plantWchAccount;
                }
            }
        }
        //确定好转账账户后封装转账信息
        Pts pts = new Pts().setPtsId(orderNumber + odId).setPayAccount(transAccount.getAccountNo()).setGetAccount(collectionAccount.getAccountNo()).setDesc("订单" + orderNumber + odId + "结束平台转账");
        if (collectionAccount == sellerAliAccount) {
            pts.setPayType("支付宝");
        } else {
            pts.setPayType("微信");
        }
        //设置转账金额
        pts.setPayMoney(orderDetailMapper.selectOrderDetailById(orderNumber, odId).getTotalAmount());
        String notifyUrl = ORDER_URL + "/pay/callback/trans";
        //进行转账
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("param", JSON.toJSONString(pts));
        multiValueMap.add("notifyUrl", notifyUrl);
        ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(ORDER_URL + "/pay/transfer", HttpMethod.PUT, new HttpEntity<>(multiValueMap, new HttpHeaders()), RpsMsg.class);
        //如果访问出错
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙，稍后再试");
        } else {
            return responseEntity.getBody();
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

    /*请求商家收款账户*/
    private SellerAccount getSellerCollectionAccount(int cate, String storeId) {
        //创建请求实体对象
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(httpHeaders);
        RpsMsg rpsMsg = restTemplate.exchange(SELLER_URL + "/store/account/receive/" + storeId+"/"+cate, HttpMethod.GET, httpEntity, RpsMsg.class).getBody();
        if (rpsMsg != null && rpsMsg.getStausCode() == 200 && rpsMsg.getData() != null) {
            //因为resttemplate返回的是一个map,通过json中间转换为我们需要的类型
            return JSONUtil.toBean(JSONUtil.parseObj(rpsMsg.getData()), SellerAccount.class);
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

