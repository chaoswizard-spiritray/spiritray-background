package spiritray.order.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import spiritray.common.pojo.DTO.OrderBeforeCommodity;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.OrderDetail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ClassName:OrderService
 * Package:spiritray.order.service
 * Description:
 *
 * @Date:2022/6/17 14:32
 * @Author:灵@email
 */
public interface OrderService {

    /*获取下单前隐藏令牌并且作为订单号*/
    public RpsMsg generateOrderToken();

    /*验证并保存订单，以及订单拆分*/
    public RpsMsg generateOrderAndDetail(List<OrderBeforeCommodity> commodities, Address address, String orderId, int payCate, long comsumerPhone, String jwt);

    /*查询订单状态*/
    public RpsMsg queryStateByOrderNumber(String orderNumber);

    /*查询信息订单*/
    public RpsMsg getOrder(long phone, int state);

    /*修改指定订单号和订单细节的订单细节记录*/
    public RpsMsg modifyOrderDetailAddressByOrderNumberAndOdId(String orderNumber, int odId, String address, HttpServletRequest request);

    /*取消指定未发货的订单细节记录*/
    public RpsMsg chanelOrderDetail(HttpServletResponse response, String orderNumber, int odId, long phone, String jwt) throws Exception;

    /*修改指定买家的指定的订单细节状态为评论发布*/
    public RpsMsg modifyOrderStateToPublish(String orderNumber, Integer odId, Long phone);

    /*确认收货*/
    public RpsMsg suerOrderdetailOver(String orderNumber, Integer odId, HttpServletResponse httpServletResponse);
}
