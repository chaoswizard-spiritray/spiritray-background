package spiritray.consumer.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.BO.CommonInf;
import spiritray.common.pojo.DTO.CartCommodity;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Cart;
import spiritray.consumer.mapper.CartMapper;
import spiritray.consumer.service.ConsumerCartService;

import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:ConsumerCartServiceImp
 * Package:spiritray.consumer.service.imp
 * Description:
 *
 * @Date:2022/4/27 19:46
 * @Author:灵@email
 */
@Service
public class ConsumerCartServiceImp implements ConsumerCartService {
    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CommonInf commonInf;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public RpsMsg quryCartCommodityInf(long phone) {
        return new RpsMsg().setStausCode(200).setData(cartMapper.selectCartByPhone(phone));
    }

    @Override
    public RpsMsg removeCartOne(long cartId) {
        if (cartMapper.deleteCart(cartId) > 0) {
            return new RpsMsg().setStausCode(200).setMsg("删除成功");
        }
        return new RpsMsg().setMsg("删除失败").setStausCode(300);
    }

    @Override
    public RpsMsg addCartOne(Cart cart) {
        if (cartMapper.insertCart(cart) > 0) {
            return new RpsMsg().setStausCode(200).setMsg("添加成功");
        }
        return new RpsMsg().setMsg("添加失败").setStausCode(300);
    }
}
