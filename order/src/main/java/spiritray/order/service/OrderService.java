package spiritray.order.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import spiritray.common.pojo.DTO.OrderBeforeCommodity;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Address;

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

    /*订单付款*/
    public RpsMsg orderPay(String orderNum, int odId);

    /*查询订单状态*/
    public RpsMsg queryStateByOrderNumber(String orderNumber);

    /*查询信息订单*/
    public RpsMsg getOrder(long phone, int state);
}
