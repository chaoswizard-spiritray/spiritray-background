package spiritray.consumer.service;

import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Cart;

/**
 * ClassName:ConsumerCartService
 * Package:spiritray.consumer.service
 * Description:
 *
 * @Date:2022/4/27 17:12
 * @Author:灵@email
 */
public interface ConsumerCartService {
    /*查询购物车中所有商品信息*/
    public RpsMsg quryCartCommodityInf(long phone);

    /*移除指定购物车信息*/
    public RpsMsg removeCartOne(long cartId);

    /*添加购物信息*/
    public RpsMsg addCartOne(Cart cart);
}
