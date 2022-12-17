package spiritray.seller.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Commodity;
import spiritray.common.pojo.PO.DownInfo;
import spiritray.common.pojo.PO.Sku;
import spiritray.seller.mapper.*;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ClassName:BeforeStoreController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/12/11 11:12
 * @Author:灵@email
 */
@RestController
@RequestMapping("/before/store")
public class BeforeStoreController {
    @Autowired
    private BeforeStoreMapper beforeStoreMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CloseInfoMapper closeInfoMapper;

    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CavMapper cavMapper;

    @Autowired
    private DownInfoMapper downInfoMapper;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private CommodityDetailMapper commodityDetailMapper;

    @Autowired
    @Qualifier("threadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private CheckInfoMapper checkInfoMapper;

    private final String PLANT_URL = "http://localhost:8083";

    private final String SELLER_URL = "http://localhost:8081";

    private final String ORDER_URL = "http://localhost:8082";

    /*查询店铺所有商品信息*/
    @GetMapping("/all/{storeId}")
    public RpsMsg getStoreAllCommoditys(@PathVariable String storeId) {
        return new RpsMsg().setData(beforeStoreMapper.selectAllCommoditysByStorejId(storeId)).setStausCode(200);
    }

    /*查询店铺近7天上架商品*/
    @GetMapping("/recent/{storeId}")
    public RpsMsg getStoreRecentCommoditys(@PathVariable String storeId) {
        return new RpsMsg().setData(beforeStoreMapper.selectRecentCommoditysByStorejId(storeId)).setStausCode(200);
    }

    /*查询店铺商品种类*/
    @GetMapping("/cate/{storeId}")
    public RpsMsg getStoreCommoditysCate(@PathVariable String storeId) {
        return new RpsMsg().setData(beforeStoreMapper.selectStoreAllCategory(storeId)).setStausCode(200);
    }

    /*根据指定种类id查询店铺商品信息*/
    @GetMapping("/all/{storeId}/{cateId}")
    public RpsMsg getStoreCommoditysCate(@PathVariable String storeId, @PathVariable Long cateId) {
        return new RpsMsg().setData(beforeStoreMapper.selectAllCommoditysByStoreIdByCateId(storeId, cateId)).setStausCode(200);
    }

    /*商家主动关闭店铺
     * 1、先检测店铺是否有未完成的订单，以及再审核中的商品，如果有就提示无法关闭
     * 2、如果没有再审核中的商品，并且所有订单均完成，就将已下架商品全部删除、然后将在售中的商品自动下架
     * 3、开启一个线程任务，两分钟后扫描订单表，看是否有该店铺的订单，如果有就执行订单取消。这里我们没有进行检测，因为用户可以自己取消订单。
     * */
    @PutMapping("/close/seller")
    public RpsMsg sellerCloseStore(HttpSession session, String cause) {
        String storeId = (String) session.getAttribute("storeId");
        //检测店铺是否没有未完成的订单
        RpsMsg rpsMsg = restTemplate.getForObject(ORDER_URL + "/order/store/check/over/" + storeId, RpsMsg.class);
        if (rpsMsg.getData() == null || (!(Boolean) rpsMsg.getData())) {
            return new RpsMsg().setStausCode(300).setMsg("不能关闭，因为店铺还有未完成订单");
        }
        //检测店铺是否有在审核中的商品，如果有就提示
        if (beforeStoreMapper.selectCountInCheckCommodity(storeId) > 0) {
            return new RpsMsg().setStausCode(300).setMsg("不能关闭，因为店铺中还有在审核中的商品");
        }
        //获取到已下架商品信息
        List<Commodity> nosellCommoditys = beforeStoreMapper.selectAllNoSellCommodityIdByStoreId(storeId);
        //删除已下架的商品
        beforeStoreMapper.deleteAllNoSellCommodityByStoreId(storeId);
        //获取还在售的商品id
//        List<String> insellIds = beforeStoreMapper.selectInSellCommodityIdsByStoreId(storeId);
        //开启线程用于删除已下架商品的属性值、sku、主图、以及删除细节照片
        threadPoolExecutor.execute(new Thread(new Runnable() {
            @Override
            public void run() {
                //循环遍历
                if (nosellCommoditys == null || nosellCommoditys.size() == 0) {
                    return;
                }
                List<String> ids = nosellCommoditys.stream().map(Commodity::getCommodityId).collect(Collectors.toList());
                //删除下架信息
                downInfoMapper.deleteDownInfoManyByCommodityId(ids);
                //将在售中的商品进行下架
                beforeStoreMapper.updateDownCommodityByStoreId(storeId);
                //循环删除信息
                nosellCommoditys.stream().peek(s -> {
                    //删除审核信息
                    checkInfoMapper.deleteCheckInfo(s.getCommodityId());
                    //删除下架信息
                    //删除属性值
                    cavMapper.deleteAllCavByCommodityId(s.getCommodityId());
                    //获取到所有sku图片路径
                    List<Sku> skuList = skuMapper.selectSkuByCommodityId(s.getCommodityId());
                    List<String> deleteImgUrl = skuList.stream().map(Sku::getSkuMap).collect(Collectors.toList());
                    //删除sku
                    skuMapper.deleteSkuByCommodityId(s.getCommodityId());
                    //删除有关照片
                    deleteImgUrl.add(s.getMasterMap());
                    if (s.getDetail() != null) {
                        deleteImgUrl.addAll(JSONUtil.toList(s.getDetail(), String.class));
                    }
                    deleteImgUrl.stream().peek(m -> {
                        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                        map.add("path", s);
                        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(map, new HttpHeaders());
                        ResponseEntity<Boolean> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, httpEntity, Boolean.class);
                    }).collect(Collectors.toList());
                }).count();
            }
        }));
        //修改店铺状态
//        storeMapper.updateStoreStatusByStoreId();
        return null;
    }

    /*查询商家主动关闭店铺信息*/
    @GetMapping("/close/info/seller/{storeId}")
    public RpsMsg getStoreSellerCloseInfo(@PathVariable String storeId) {
        return new RpsMsg().setStausCode(200).setData(beforeStoreMapper.selectSellerCloseInfo(storeId));
    }

    /*查询指定店铺平台查封信息*/
    @GetMapping("/close/info/plant/{storeId}")
    public RpsMsg getStorePlantCloseInfo(@PathVariable String storeId) {
        return new RpsMsg().setStausCode(200).setData(closeInfoMapper.selectCloseInfoByStoreId(storeId));
    }
}
