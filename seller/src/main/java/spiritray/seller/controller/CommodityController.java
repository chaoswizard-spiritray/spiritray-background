package spiritray.seller.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.DTO.CommodityCondition;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.*;
import spiritray.seller.mapper.*;
import spiritray.seller.service.CommodityService;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.Date;
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

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private CavMapper cavMapper;

    @Autowired
    private DownInfoMapper downInfoMapper;

    /*商家重新申请审核商品*/
    @PutMapping("/seller/check/reapply")
    @Transactional(rollbackFor = Exception.class)
    public RpsMsg reapplyCheckCommodity(String commodityId, HttpServletResponse response) throws Exception {
        try {
            //修改商品审核状态
            checkInfoMapper.updateCheckInfoCodeAndClear(commodityId);
            return new RpsMsg().setMsg("申请成功").setStausCode(200);
        } catch (Exception e) {
            Writer writer = response.getWriter();
            writer.write(JSON.toJSONString(new RpsMsg().setMsg("申请失败，请稍后再试!").setStausCode(300)));
            writer.flush();
            writer.close();
            throw new Exception();
        }
    }

    /*商家移除未通过审核的商品*/
    @PutMapping("/seller/check/remove")
    @Transactional(rollbackFor = Exception.class)
    public RpsMsg removeNoPassCheckCommodity(String commodityId, HttpServletResponse response) throws Exception {
        try {
            //移除商品的所有属性
            cavMapper.deleteAllCavByCommodityId(commodityId);
            //移除所有sku
            skuMapper.deleteSkuByCommodityId(commodityId);
            //移除商品的审核信息
            checkInfoMapper.deleteCheckInfo(commodityId);
            //移除商品信息
            commodityMapper.deleteCommodityByCommodityId(commodityId);
            return new RpsMsg().setMsg("移除成功").setStausCode(200);
        } catch (Exception e) {
            Writer writer = response.getWriter();
            writer.write(JSON.toJSONString(new RpsMsg().setMsg("移除失败，稍后再试").setStausCode(300)));
            writer.flush();
            writer.close();
            throw new Exception();
        }
    }

    /*商家主动下架在售中的商品*/
    @PostMapping("/seller/down")
    @Transactional(rollbackFor = Exception.class)
    public RpsMsg downCommodity(String commodityId, String des) {
        //修改商品状态
        commodityMapper.updateCommodityState(-1, commodityId);
        //保存下架信息
        downInfoMapper.insertDownInfoOne(new DownInfo().setCommodityId(commodityId).setStaff(-1L).setDownDes(des).setDownDate(new Timestamp(new Date().getTime())));
        return new RpsMsg().setStausCode(200).setMsg("商品下架成功");
    }

    /*批量查询商品的名称*/
    @PutMapping("/commodityName")
    public RpsMsg getCommodityNameMul(String ids) {
        List<String> commodityIds = JSON.parseArray(ids).toJavaList(String.class);
        return new RpsMsg().setStausCode(200).setData(commodityMapper.selectCommodityName(commodityIds));
    }

    /*批量检测商品的状态是否都是在售中*/
    @GetMapping("/check/state")
    public RpsMsg checkCommoditysStateExistNoSell(@RequestParam("commodityIds") String commodityIds) {
        List<String> ids = JSONUtil.toList(commodityIds, String.class);
        Long count = commodityMapper.selectCountInSellByCommodityIds(ids);
        if (count != ids.size()) {
            //如果数目小于长度
            return new RpsMsg().setStausCode(200).setData(false);
        } else {
            return new RpsMsg().setStausCode(200).setData(true);
        }
    }

    /*查询指定商品id的商品信息*/
    @GetMapping("/order/{commodityId}")
    public RpsMsg getCommodityById(@PathVariable String commodityId) {
        return new RpsMsg().setStausCode(200).setData(commodityMapper.selectCommodityById(commodityId));
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

    /*查询商品下架详细信息*/
    @GetMapping("/nosell/detail/{commodityId}")
    public RpsMsg getNoSellDetail(HttpSession session, @PathVariable String commodityId) {
        return commodityService.queryCommodityById((String) session.getAttribute("storeId"), commodityId, false, -1);
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
