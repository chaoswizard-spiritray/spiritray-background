package spiritray.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.OrderDetail;

import java.util.List;
import java.util.Map;

/**
 * ClassName:OrderDetailMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/6/18 22:57
 * @Author:灵@email
 */
@Mapper
@Repository
public interface OrderDetailMapper {
    /*删除订单细节记录根据key*/
    public int updateDetailDeleteById(@Param("orderNumber") String orderNumber, @Param("odId") int odId);

    /*查询指定用户指定指定状态的信息*/
    public List<OrderDetail> selectOrderDetailByPhoneAndState(@Param("phone") long phone, @Param("state") int state);

    /*查询指定编号的商品信息*/
    public Map<String, Object> selectOrderSkuByOrderId(@Param("orderNumber") String orderNumber, @Param("odId") int odId);

    /*查询指定订单细节编号的状态*/
    public int selectDetailStateById(@Param("orderNumber") String orderNumber, @Param("odId") int odId);

    /*查询指定订单编号的的第一条订单细节付款状态*/
    public int selectDetailStateByOrderNumber(@Param("orderNumber") String orderNumber);

    /*插入批量订单细节信息*/
    public int insertOrderDetail(@Param("orderDetails") List<OrderDetail> orderDetails);

    /*修改指定订单编号的所有订单细节记录的状态*/
    public int updateDetailByOrderNum(@Param("orderNumber") String orderNumber, @Param("state") int state);

    /*修改指定订单细节记录状态*/
    public int updateDetailStateById(@Param("orderNumber") String orderNumber, @Param("odId") int odId, @Param("state") int state);
}
