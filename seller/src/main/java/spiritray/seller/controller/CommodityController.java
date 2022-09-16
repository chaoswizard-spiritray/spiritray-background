package spiritray.seller.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.DTO.CommodityCondition;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Cav;
import spiritray.common.pojo.PO.CheckInfo;
import spiritray.common.pojo.PO.Commodity;
import spiritray.common.pojo.PO.Sku;
import spiritray.seller.mapper.CheckInfoMapper;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.service.CommodityService;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ClassName:CommodityController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/4/26 13:25
 * @Author:灵@email
 */
@RestController
@RequestMapping("/commodity")
public class CommodityController {
    @Autowired
    private CommodityService commodityService;

    @Autowired
    private CheckInfoMapper checkInfoMapper;

    @Autowired
    private CommodityMapper commodityMapper;

    /*批量查询商品的名称*/
    @PutMapping("/commoidtyName")
    public RpsMsg getCommodityNameMul(String ids) {
        List<String> commodityIds = JSON.parseArray(ids).toJavaList(String.class);
        return new RpsMsg().setData(commodityMapper.selectCommodityName(commodityIds));
    }

    /*发布商品信息*/
    @PostMapping("/publish")
    public RpsMsg publishCommodity(HttpSession session, Commodity commodityInf, @RequestParam("cavs") String cavs, @RequestParam("skus") String skus, MultipartFile masterMapFile, @RequestParam("salveMapFiles") List<MultipartFile> salveMapFiles) {
        List<Cav> cavList = JSONArray.parseArray(cavs).toJavaList(Cav.class);
        List<Sku> skuList = JSONArray.parseArray(skus).toJavaList(Sku.class);
        commodityInf.setStoreId((String) session.getAttribute("storeId"));
        return commodityService.publishCommodity(cavList, commodityInf, skuList, masterMapFile, salveMapFiles);
    }

    /*查询当前商家已上架商品简略信息*/
    @GetMapping("/insell/simple")
    public RpsMsg getInSellSimple(HttpSession session) {
        return commodityService.queryCommodityById(String.valueOf(session.getAttribute("storeId")), null, true, 1);
    }

    /*查询当前商家已上架商品详细信息*/
    @GetMapping("/insell/detail/{commodityId}")
    public RpsMsg getInSellDetail(@PathVariable String commodityId) {
        return commodityService.queryCommodityById(null, commodityId, false, 1);
    }

    /*查询商家待审核商品简略信息*/
    @GetMapping("/incheck/simple")
    public RpsMsg getInCheckSimple(HttpSession session) {
        return commodityService.queryCommodityById(String.valueOf(session.getAttribute("storeId")), null, true, 0);
    }

    /*查询商家待审核商品详细信息*/
    @GetMapping("/incheck/detail/{commodityId}")
    public RpsMsg getInCheckDetail(HttpSession session, @PathVariable String commodityId) {
        return commodityService.queryCommodityById((String) session.getAttribute("storeId"), commodityId, false, 0);
    }

    /*查询商家已下架商品简略信息*/
    @GetMapping("/nosell/simple")
    public RpsMsg getNoSellSimple(HttpSession session) {
        return commodityService.queryCommodityById(String.valueOf(session.getAttribute("storeId")), null, true, -1);
    }

    /*查询商品买家简略信息*/
    @GetMapping("/consumer/simple")
    public RpsMsg getConsumerSimple(CommodityCondition commodityCondition) {
        return commodityService.queryCommodityConsumerSimple(commodityCondition);
    }

    /*查询平台商品审核信息简洁信息*/
    @GetMapping("/plat/check/simple/{state}")
    public RpsMsg getCommodityPlatCheckinfo(@PathVariable int state) {
        return commodityService.queryCommodityPlatCheckSimple(state);
    }

    /*商品通过审核*/
    @PutMapping("/plat/check/pass")
    public RpsMsg pass(String commodityId, String remark, int code, HttpSession session) {
        CheckInfo checkInfo = new CheckInfo();
        checkInfo.setCommodityId(commodityId).setRemark(remark).setCheckCode(code).setStaffId((Long) session.getAttribute("staffId"));
        return commodityService.passCheck(checkInfo);
    }

    /*拒绝商品发布*/
    @PutMapping("/plat/check/refocus")
    public RpsMsg refocus(String commodityId, String remark, int code, HttpSession session) {
        CheckInfo checkInfo = new CheckInfo();
        checkInfo.setCommodityId(commodityId).setRemark(remark).setCheckCode(code).setStaffId((Long) session.getAttribute("staffId"));
        return commodityService.refocusCheck(checkInfo);
    }

    /*获取商品审核信息*/
    @GetMapping("/plat/check/info/{commodityId}")
    public RpsMsg getInfo(@PathVariable String commodityId) {
        return new RpsMsg().setStausCode(200).setData(checkInfoMapper.selectCheckInfoExtend(commodityId));
    }

}
