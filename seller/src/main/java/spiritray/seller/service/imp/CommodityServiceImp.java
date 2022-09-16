package spiritray.seller.service.imp;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.*;
import spiritray.common.pojo.PO.Cav;
import spiritray.common.pojo.PO.CheckInfo;
import spiritray.common.pojo.PO.Commodity;
import spiritray.common.pojo.PO.Sku;
import spiritray.common.tool.FileUpLoadTool;
import spiritray.seller.mapper.CavMapper;
import spiritray.seller.mapper.CheckInfoMapper;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.mapper.SkuMapper;
import spiritray.seller.service.CommodityService;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * ClassName:CommodityServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 *
 * @Date:2022/4/26 21:15
 * @Author:灵@email
 */
@Service
@Slf4j
public class CommodityServiceImp implements CommodityService {
    @Autowired
    private CavMapper cavMapper;

    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CheckInfoMapper checkInfoMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private HttpHeaders httpHeaders;

    private String orderUrl = "http://localhost:8082";


    @SneakyThrows
    @Transactional
    @Override
    public RpsMsg publishCommodity(List<Cav> cavs, Commodity commodity, List<Sku> skus, MultipartFile masterMap, List<MultipartFile> salveMap) {
        //生成随机商品编号
        String commodityId = String.valueOf(UUID.randomUUID());
        //设置商品信息
        commodity.setCommodityId(commodityId);
        //设置属性值信息
        for (Cav cav : cavs) {
            cav.setCommodityId(commodityId);
        }
        //封装主图上传信息
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(masterMap.getInputStream(), masterMap.getSize(), masterMap.getOriginalFilename());
        param.add("file", commonInputStreamResource);
        param.add("path", "/static/commodity/master");
        param.add("fileName", masterMap.getOriginalFilename());
        HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
        String masterUrl = null;
        try {
            masterUrl = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class).getBody();
        } catch (Exception e) {
            return new RpsMsg().setStausCode(300).setMsg("发布失败，商品主图已损坏或文件服务不可用");
        }
        if (masterUrl == null) {
            return new RpsMsg().setMsg("发布失败，商品主图已损坏或文件服务不可用");
        }
        commodity.setMasterMap(masterUrl);
        //封装附图上传信息
        param.remove("file");
        param.set("path", "/static/commodity/salve");
        for (MultipartFile multipartFile : salveMap) {
            CommonInputStreamResource commonInputStreamResource1 = new CommonInputStreamResource(multipartFile.getInputStream(), multipartFile.getSize(), multipartFile.getOriginalFilename());
            param.add("files", commonInputStreamResource1);
        }
        param.add("isBack", true);
        //因为httpEntity的属性是final，所以内部持有的是单例的，我们可以改变参数表单的值，对这个请求实体进行复用
        FileUploadMsg fileUploadMsg = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_MUL(), HttpMethod.POST, httpEntity, FileUploadMsg.class).getBody();
        //设置sku信息
        List<String> salveUrls = fileUploadMsg.getFilePaths();
        int i = 0;
        for (Sku sku : skus) {
            sku.setCommodityId(commodityId);
            sku.setSkuMap(salveUrls.get(i++));
        }
        //保存商品基本信息
        int totalI = commodityMapper.insertCommodity(commodity);
        //保存商品属性值
        int totalJ = cavMapper.insertCavS(cavs);
        //初始化审核信息
        checkInfoMapper.insertCheckInfo(commodityId);
        //保存商品sku信息
        int totalK = skuMapper.insertSkus(skus);
        if (totalI > 0 && totalJ > 0 && totalK > 0) {
            return new RpsMsg().setStausCode(200).setMsg("发布成功，等待审核，如果没有相关执照，请尽快上传");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("发布失败，请稍后再试");
        }
    }

    @Override
    public RpsMsg queryCommodityById(String storeId, String commodityId, boolean isSimple, int commodityState) {
        if (isSimple) {
            switch (commodityState) {
                case -1: {
                    //查询已下架信息简略信息
                    return new RpsMsg().setStausCode(200).setData(commodityMapper.selectNoSellSimpleByStoreId(storeId));
                }
                case 1: {
                    //查询售卖中的简略信息
                    List<InSellSimple> commditys = commodityMapper.selectInSellSimpleByStoreId(storeId);
                    //查询所有商品当月售卖量
                    RpsMsg msg = restTemplate.getForObject(orderUrl + "/order/count/month/" + storeId, RpsMsg.class);
                    //封装信息
                    Map<String, Object> data = new HashMap<>();
                    data.put("commoditys", commditys);
                    data.put("sellTotals", msg.getData());
                    return new RpsMsg().setStausCode(200).setData(data);
                }
                case 0: {
                    return new RpsMsg().setStausCode(200).setData(commodityMapper.selectInCheckSimpleByStoreId(storeId));
                }
                default: {
                    return null;
                }
            }
        } else {
            switch (commodityState) {
                case 0: {
                    return new RpsMsg().setStausCode(200).setData(commodityMapper.selectInCheckDetailByCommodityId(storeId, commodityId));
                }
                case 1: {
                    return new RpsMsg().setStausCode(200).setData(commodityMapper.selectInSellDetailByCommodityId(commodityId));
                }
                default: {
                    return new RpsMsg().setStausCode(300).setMsg("无信息");
                }
            }
        }
    }

    @Override
    public RpsMsg queryCommodityConsumerSimple(CommodityCondition commodityCondition) {
        return new RpsMsg().setMsg("查询成功").setStausCode(200).setData(commodityMapper.selectCommodityConsumerSimple(commodityCondition));
    }

    @Override
    public RpsMsg queryCommodityPlatCheckSimple(int state) {
        return new RpsMsg().setData(commodityMapper.selectCommoditySimpleByCheckState(state)).setMsg("查询成功").setStausCode(200);
    }

    @Override
    @Transactional(rollbackFor = {IllegalArgumentException.class})
    public RpsMsg passCheck(CheckInfo checkInfo) {
        //修改商品状态
        int i = commodityMapper.updateCommodityState(1, checkInfo.getCommodityId());
        //修改审核信息
        int j = checkInfoMapper.updateCheckInfo(checkInfo);
        if (i > 0 && j > 0) {
            return new RpsMsg().setStausCode(200).setMsg("通过成功");
        } else {
            throw new IllegalArgumentException("更新操作逻辑有误");
        }
    }

    @Override
    public RpsMsg refocusCheck(CheckInfo checkInfo) {
        //修改审核信息
        int j = checkInfoMapper.updateCheckInfo(checkInfo);
        if (j > 0) {
            return new RpsMsg().setStausCode(200).setMsg("拒绝成功");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("拒绝失败");
        }
    }

}
