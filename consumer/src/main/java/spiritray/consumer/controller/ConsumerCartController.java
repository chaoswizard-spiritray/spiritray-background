package spiritray.consumer.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Cart;
import spiritray.consumer.mapper.CartMapper;
import spiritray.consumer.service.ConsumerCartService;

import javax.servlet.http.HttpSession;

/**
 * ClassName:ConsumerCartController
 * Package:spriritray.consumer.controller
 * Description:
 * 买家购物车相关操作
 *
 * @Date:2022/3/30 3:39
 * @Author:灵@email
 */
@RestController
@RequestMapping("/consumer/cart")
public class ConsumerCartController {
    @Autowired
    private ConsumerCartService consumerCartService;

    @Autowired
    private CartMapper cartMapper;

    /*查询购物车所有商品信息*/
    @RequestMapping(value = "/commoditys", method = RequestMethod.GET)
    public RpsMsg getCommoditys(HttpSession session) {
        return consumerCartService.quryCartCommodityInf((Long) session.getAttribute("phone"));
    }

    /*添加商品到购物车*/
    @RequestMapping(value = "/commoditys", method = RequestMethod.POST)
    public RpsMsg addCommodityToCart(String cart, HttpSession session) {
        Cart cart1 = JSON.parseObject(cart, Cart.class);
        cart1.setConsumerPhone((Long) session.getAttribute("phone"));
        return consumerCartService.addCartOne(cart1);
    }

    /*删除购物车的商品*/
    @RequestMapping(value = "/commoditys", method = RequestMethod.PUT)
    public RpsMsg removeCommoditysInCart(long cartId) {
        return consumerCartService.removeCartOne(cartId);
    }

    /*清空购物车*/
    @PutMapping("/clear")
    public RpsMsg clearCart(HttpSession session) {
        if (cartMapper.deleteAll((Long) session.getAttribute("phone")) > 0) {
            return new RpsMsg().setStausCode(200).setMsg("已清空");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("清空失败");
        }
    }

}
