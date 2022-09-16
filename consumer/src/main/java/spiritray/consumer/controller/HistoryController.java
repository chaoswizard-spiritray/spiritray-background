package spiritray.consumer.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.History;
import spiritray.consumer.mapper.HistoryMapper;

import javax.servlet.http.HttpSession;

/**
 * ClassName:HistoryController
 * Package:spiritray.consumer.controller
 * Description:
 *
 * @Date:2022/6/15 15:13
 * @Author:灵@email
 */
@RestController
@RequestMapping("/history")
public class HistoryController {
    @Autowired
    private HistoryMapper historyMapper;

    /*获取指定用户最近浏览时间最长的商品*/
    @GetMapping("/plat/{phone}/{num}")
    public RpsMsg getRecentlyByPhone(@PathVariable("phone") long phone, @PathVariable("num") int num) {
        return new RpsMsg().setStausCode(200).setData(historyMapper.selectLookRecentlyLong(phone, num));
    }

    /*记录用户浏览历史信息*/
    @PostMapping("/add")
    public RpsMsg addHistory(int lookTime, String commodityId, HttpSession session) {
        History history = new History().setCommodityId(commodityId).setLookTime(lookTime);
        history.setConsumerPhone((Long) session.getAttribute("phone"));
        //先判断记录存不存在
        if (historyMapper.selectHisByPhoneAndCommodityId((Long) session.getAttribute("phone"), history.getCommodityId()) != null) {
            //存在直接更新
            historyMapper.updateHisByPhoneAndCommodityId(history);
            return new RpsMsg().setStausCode(200).setMsg("操作成功");
        } else {
            //不存在就插入
            historyMapper.insertHisOne(history);
            return new RpsMsg().setStausCode(200).setMsg("操作成功");
        }
    }


    /*删除浏览历史*/
    @DeleteMapping("/remove")
    public RpsMsg removeHistory(String commodityId, HttpSession session) {
        historyMapper.deleteHisByPhoneAndCommodityId((Long) session.getAttribute("phone"), commodityId);
        return new RpsMsg().setStausCode(200).setMsg("删除成功");
    }


}
