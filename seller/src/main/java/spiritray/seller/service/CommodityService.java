package spiritray.seller.service;

import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.DTO.CommodityCondition;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Cav;
import spiritray.common.pojo.PO.CheckInfo;
import spiritray.common.pojo.PO.Commodity;
import spiritray.common.pojo.PO.Sku;

import java.util.List;

/**
 * ClassName:CommodityService
 * Package:spiritray.seller.service
 * Description:
 *
 * @Date:2022/4/26 21:14
 * @Author:灵@email
 */
public interface CommodityService {
    /*添加商品信息*/
    public RpsMsg publishCommodity(List<Cav> cavs, Commodity commodity, List<Sku> skus, MultipartFile masterMap, List<MultipartFile> salveMap);

    /*查询店铺商品信息*/
    public RpsMsg queryCommodityById(String storeId, String commodityId, boolean isSimple, int commodityState);

    /*商家视图查询商品信息*/
    public RpsMsg queryCommodityConsumerSimple(CommodityCondition commodityCondition);

    /*查询平台商品审核信息*/
    public RpsMsg queryCommodityPlatCheckSimple(int state);

    /*通过商品审核*/
    public RpsMsg passCheck(CheckInfo checkInfo);

    /*商品发布拒绝*/
    public RpsMsg refocusCheck(CheckInfo checkInfo);
}
