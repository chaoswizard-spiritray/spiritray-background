package spiritray.seller.service;

import spiritray.common.pojo.DTO.RpsMsg;

/**
 * ClassName:CavService
 * Package:spiritray.seller.service
 * Description:
 *
 * @Date:2022/4/26 13:37
 * @Author:灵@email
 */
public interface CavService {
    /*查询商品的种类*/
    public RpsMsg queryCategory(int categoryId);

    /*查询种类属性*/
    public RpsMsg queryAttribute(int categoryId);

    /*查询商品的多值属性*/
    public RpsMsg queryCavByCommodityId(String commodityId,boolean isMul);

    /*查询商品的所有属性以及值*/
    public RpsMsg queryAllCavCommodityId(String commodityId);

}
