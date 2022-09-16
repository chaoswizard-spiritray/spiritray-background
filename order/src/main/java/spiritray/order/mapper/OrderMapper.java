package spiritray.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Order;

/**
 * ClassName:OrderMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/6/18 22:52
 * @Author:灵@email
 */
@Mapper
@Repository
public interface OrderMapper {
    /*插入订单信息*/
    public int insertOrder(@Param("order") Order order);

    /*清除无订单细节的订单*/
    public int updateOrderNoDetail();
}
