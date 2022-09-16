package spiritray.seller.service;

import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;

import java.util.List;

/**
 * ClassName:SkuService
 * Package:spiritray.seller.service
 * Description:
 *
 * @Date:2022/6/14 0:02
 * @Author:灵@email
 */
public interface SkuService {

    /*查询指定商品的sku*/
    public RpsMsg queryCommoditySku(String commodityId);

    /*比对查询检查信息参数*/
    public RpsMsg queryCheckOrderInfo(List<SSMap> checkParams);

    /*减少指定商品规格指定数量，可以添加版本字段，解决加锁的问题*/
    public RpsMsg updateSkuNum(List<SSMap> checkParams, List<Integer> nums);

    /*修改指定商品数量*/
    public RpsMsg addSkuNum(String commodityId, String skuValue, int num);
}
