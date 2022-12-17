package spiritray.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Attention;
import spiritray.consumer.mapper.AttentionMapper;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Date;

/**
 * ClassName:AttentionController
 * Package:spiritray.consumer.controller
 * Description:
 *
 * @Date:2022/12/11 11:23
 * @Author:灵@email
 */
@RestController
@RequestMapping("/consumer/attention")
public class AttentionController {
    @Autowired
    private AttentionMapper attentionMapper;

    @GetMapping("/{storeId}")
    public RpsMsg checkConsumerIsAttentionStore(@PathVariable String storeId, HttpSession session) {
        Long phone = (Long) session.getAttribute("phone");
        if (phone == null) {
            return new RpsMsg().setStausCode(300).setMsg("请先登录");
        } else {
            Attention attention = attentionMapper.selectAttentionByConsumerPhoneAndStoreId(storeId, phone);
            return new RpsMsg().setStausCode(200).setData(attention);
        }
    }

    @PostMapping("/{storeId}")
    public RpsMsg addAttention(@PathVariable String storeId, HttpSession session) {
        Long phone = (Long) session.getAttribute("phone");
        if (phone == null) {
            return new RpsMsg().setStausCode(300).setMsg("请先登录");
        } else {
            Attention attention = new Attention().setConsumerPhone(phone).setStartDate(new Timestamp(new Date().getTime())).setStoreId(storeId);
            if (attentionMapper.insertAttention(attention) == 1) {
                return new RpsMsg().setStausCode(200).setMsg("关注成功");
            } else {
                return new RpsMsg().setStausCode(200).setMsg("关注失败");
            }
        }
    }

    @PutMapping("/{storeId}")
    public RpsMsg deleteAttention(@PathVariable String storeId, HttpSession session) {
        Long phone = (Long) session.getAttribute("phone");
        if (phone == null) {
            return new RpsMsg().setStausCode(300).setMsg("请先登录");
        } else {
            try {
                if (attentionMapper.deleteAttentionByConsumerPhoneAndStoreId(storeId, phone) == 1) {
                    return new RpsMsg().setStausCode(200).setMsg("已取消");
                }
                return new RpsMsg().setStausCode(200).setMsg("取消失败");
            } catch (Exception e) {
                return new RpsMsg().setStausCode(200).setMsg("取消失败");
            }
        }
    }

    @GetMapping("/consumer/all")
    public RpsMsg getConsumerAllAttention(HttpSession session) {
        Long phone = (Long) session.getAttribute("phone");
        if (phone == null) {
            return new RpsMsg().setStausCode(300).setMsg("请先登录");
        } else {
            return new RpsMsg().setStausCode(200).setData(attentionMapper.selectConsumerAllAttention(phone));
        }
    }

    @GetMapping("/store/count/{storeId}")
    public RpsMsg getStoreAttentionCount(@PathVariable String storeId) {
        return new RpsMsg().setStausCode(200).setData(attentionMapper.selectStoreAttentionNum(storeId));
    }

    @GetMapping("/store/all/{storeId}")
    public RpsMsg getStoreAllAttention(@PathVariable String storeId) {
        return new RpsMsg().setStausCode(200).setData(attentionMapper.selectStoreAllAttention(storeId));
    }
}
