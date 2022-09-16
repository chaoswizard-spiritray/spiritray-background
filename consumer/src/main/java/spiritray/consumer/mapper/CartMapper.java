package spiritray.consumer.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Cart;

import java.util.List;

/**
 * ClassName:CartMapper
 * Package:spiritray.consumer.mapper
 * Description:
 *
 * @Date:2022/4/27 17:27
 * @Author:灵@email
 */
@Repository
public interface CartMapper {
    /*根据用户电话查询购物车所有商品*/
    public List<Cart> selectCartByPhone(@Param("phone") long phone);

    /*添加商品到购物车*/
    public int insertCart(@Param("cart") Cart cart);

    /*根据id删除购物车记录*/
    public int deleteCart(@Param("cartId") long cartId);

    /*删除所有记录*/
    public int deleteAll(@Param("phone") long phone);
}
