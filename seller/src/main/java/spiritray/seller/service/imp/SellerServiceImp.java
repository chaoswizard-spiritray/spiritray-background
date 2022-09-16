package spiritray.seller.service.imp;

import cn.hutool.core.codec.Base64;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInf;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.RspMsgAPI;
import spiritray.common.pojo.PO.Seller;
import spiritray.common.pojo.PO.Store;
import spiritray.common.tool.RealNameTool;
import spiritray.seller.mapper.SellerMapper;
import spiritray.seller.mapper.StoreMapper;
import spiritray.seller.service.SellerService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * ClassName:SellerServiceImp
 * Package:spiritray.consumer.service.imp
 * Description:
 *
 * @Date:2022/4/17 16:05
 * @Author:灵@email
 */
@Service
public class SellerServiceImp implements SellerService {

    @Autowired
    private SellerMapper sellerMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private CommonInf commonInf;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private HttpHeaders httpHeaders;

    @Override
    public RpsMsg checkEnter(long phone) {
        RpsMsg rpsMsg = new RpsMsg();
        if (sellerMapper.selectPhoneByPhone(phone) == null) {
            return rpsMsg.setMsg("未入驻").setStausCode(201);
        } else {
            return rpsMsg.setStausCode(200).setMsg("已入驻");
        }
    }

    @SneakyThrows
    @Override
    public RpsMsg enter(Seller seller, MultipartFile file) {
        //先验证是否已经入驻
        if (checkEnter(seller.getSellerPhone()).getStausCode() == 200) {
            return new RpsMsg().setMsg("已入驻，务重复操作").setStausCode(301);
        }
        if (file == null) {
            return new RpsMsg().setMsg("未指定身份证头像面图片").setStausCode(300);
        }
        //准备调用接口实名认证
        Map<String, String> map = new HashMap();
        map.put("idcard", seller.getSellerId());
        map.put("name", seller.getSellerName());
        String value = Base64.encode(file.getBytes());
        map.put("image", "data:image/jpeg;base64," + value);
        //调用实名认证接口
//        RspMsgAPI rspMsgAPI = null;
//        try {
//            rspMsgAPI = RealNameTool.realName(commonInf.getRealNameUrl(), commonInf.getRealNameCode(), map);
//        } catch (Exception e) {
//            return new RpsMsg().setMsg("认证失败,认证服务不可用，稍后再试").setStausCode(300);
//        }
//        //等待验证信息
//        if (rspMsgAPI.getCode() == 200 && new Integer(100).equals(rspMsgAPI.getData().get("incorrect"))) {
//            //请求参数表单
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        //文件输入流
        CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
        //添加参数到表单
        param.add("file", commonInputStreamResource);
        param.add("path", "/static/seller/id");
        param.add("fileName", seller.getSellerId());
        //创建请求实体
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, httpHeaders);
        //上传成功后在文件服务中文件的路径
        String filePath = null;
        try {
            //除了get以外，请求统一使用exchange
            ResponseEntity<String> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class);
            filePath = responseEntity.getBody();
        } catch (Exception e) {
            return new RpsMsg().setStausCode(300).setMsg("文件上传失败,请稍后重新认证");
        }
        if (filePath == null) {
            return new RpsMsg().setStausCode(300).setMsg("认证失败");
        } else {
            //如果文件已经存储那么就将商家信息保存
            seller.setSellerPath(filePath);
            //添加信息
            if (sellerMapper.insertSeller(seller) > 0) {
                //插入成功,接着生成默认店铺信息
                Store store = new Store()
                        .setStoreId(String.valueOf(UUID.randomUUID()))
                        .setStoreName(String.valueOf(seller.getSellerPhone()))
                        .setStoreHead("/static/store/head/default.jpeg")
                        .setSellerId(seller.getSellerId())
                        .setStatus(1);
                //添加店铺信息
                if (storeMapper.insertStore(store) > 0) {
                    store.setSellerId(null);
                    return new RpsMsg().setStausCode(200).setMsg("认证通过").setData(store);
                } else {
                    return new RpsMsg().setStausCode(300).setMsg("认证失败，请重新认证");
                }
            } else {
                return new RpsMsg().setStausCode(300).setMsg("认证失败，请重新认证");
            }
        }
//        } else {
//            //其他情况返回认证失败
//            return new RpsMsg().setStausCode(300).setMsg("认证失败，请重新认证");
//        }
    }
}
