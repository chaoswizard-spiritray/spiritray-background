package spiritray.seller.service;

import spiritray.common.pojo.DTO.RpsMsg;

/**
 * ClassName:ConsumerCommodityService
 * Package:spiritray.seller.service
 * Description:
 *
 * @Date:2022/6/15 9:21
 * @Author:灵@email
 */
public interface ConsumerCommodityService {
    /*获取客户端首页商品*/
    public RpsMsg queryHomeCommodity(int pageNum, int recordNum, long phone);

    /*查询客户端商品详情*/
    public RpsMsg queryConsumerCommodityDetail(String commodityId);

    /*查询买家搜索商品信息*/
    public RpsMsg queryConsumerCommoditySearch(String word);
}
