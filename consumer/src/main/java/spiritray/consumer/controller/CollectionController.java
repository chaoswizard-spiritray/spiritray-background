package spiritray.consumer.controller;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.CommodityCollection;
import spiritray.consumer.mapper.CollectionMapper;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ClassName:Collection
 * Package:spiritray.consumer.controller
 * Description:
 *
 * @Date:2022/6/15 14:36
 * @Author:灵@email
 */
@RestController
@RequestMapping("/collection")
public class CollectionController {
    @Autowired
    private CollectionMapper collectionMapper;

    /*获取买家商品收藏信息*/
    @GetMapping("/consumer/all")
    public RpsMsg getConsumerAllCollection(HttpSession session) {
        return new RpsMsg().setStausCode(200).setData(collectionMapper.selectAllCollection((Long) session.getAttribute("phone")));
    }

    /*批量删除收藏信息*/
    @PutMapping("/many/delete")
    public RpsMsg deleteManyCollection(String commodityIds, HttpSession session) {
        List<String> ids = JSONUtil.toList(commodityIds, String.class);
        ids.stream().peek(s -> {
            collectionMapper.deleteCollectionOne(new CommodityCollection().setCommodityId(s).setConsumerPhone((Long) session.getAttribute("phone")));
        }).count();
        return new RpsMsg().setStausCode(200).setMsg("移除成功");
    }

    /*平台拿取商品收藏信息*/
    @GetMapping("/plat/{phone}")
    public RpsMsg getCollectionOrderByDate(@PathVariable long phone) {
        return new RpsMsg().setData(collectionMapper.selectCollectionCommodityIdByPhone(phone)).setStausCode(200);
    }

    /*查询当前用户是否收藏了该商品*/
    @GetMapping("/simple/{commodityId}")
    public RpsMsg getIsCollectionByPhone(@PathVariable String commodityId, HttpSession session) {
        try {
            if (collectionMapper.selectIsCollectionByPhoneAndCommodityId((Long) session.getAttribute("phone"), commodityId) == null)
                return new RpsMsg().setData(false).setStausCode(200);
            else return new RpsMsg().setData(true).setStausCode(200);
        } catch (Exception e) {
            return new RpsMsg().setData(false).setStausCode(200);
        }
    }

    /*修改收藏状态，1收藏，0取消收藏*/
    @PutMapping("/simple")
    public RpsMsg putCollection(String commodityId, int state, HttpSession session) {
        if (session.getAttribute("phone") == null) {
            return new RpsMsg().setStausCode(400).setMsg("还没有登录");
        }
        if (state == 1) {
            if (collectionMapper.insertCollectionOne(new CommodityCollection().setCommodityId(commodityId).setConsumerPhone((Long) session.getAttribute("phone"))) > 0) {
                return new RpsMsg().setMsg("收藏成功").setStausCode(200).setData(true);
            } else {
                return new RpsMsg().setMsg("收藏失败").setStausCode(200).setData(false);
            }
        } else {
            if (collectionMapper.deleteCollectionOne(new CommodityCollection().setCommodityId(commodityId).setConsumerPhone((Long) session.getAttribute("phone"))) > 0) {
                return new RpsMsg().setMsg("取消成功").setStausCode(200).setData(false);
            } else {
                return new RpsMsg().setMsg("取消失败").setStausCode(200).setData(true);
            }
        }
    }
}
