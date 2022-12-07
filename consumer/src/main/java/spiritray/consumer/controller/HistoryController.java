package spiritray.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.History;
import spiritray.consumer.mapper.HistoryMapper;

import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        return new RpsMsg().setStausCode(200).setData(historyMapper.selectLookRecentlyLongCommodityIdAndNoDelete(phone, num));
    }

    /*记录用户浏览历史信息*/
    @PostMapping("/add")
    public RpsMsg addHistory(int lookTime, String commodityId, HttpSession session) {
        History history = new History().setCommodityId(commodityId).setLookTime(lookTime);
        history.setConsumerPhone((Long) session.getAttribute("phone"));
        //先判断记录存不存在
        if (historyMapper.selectNoDeleteHisByPhoneAndCommodityId((Long) session.getAttribute("phone"), history.getCommodityId()) != null) {
            //存在直接更新
            historyMapper.updateHisByPhoneAndCommodityId(history);
            return new RpsMsg().setStausCode(200).setMsg("操作成功");
        } else {
            //不存在就插入
            history.setHisId(String.valueOf(UUID.randomUUID()));
            historyMapper.insertHisOne(history);
            return new RpsMsg().setStausCode(200).setMsg("操作成功");
        }
    }


    /*删除浏览历史*/
    @DeleteMapping("/remove")
    public RpsMsg removeHistory(String commodityId, HttpSession session) {
        historyMapper.updateIsDeleteHisByPhoneAndCommodityId((Long) session.getAttribute("phone"), commodityId);
        return new RpsMsg().setStausCode(200).setMsg("删除成功");
    }


    /*获取买家商品首页推荐所需商品id
     * 浏览记录：全表未删除记录取最近10天的浏览记录，按照浏览时间、
     * 浏览时长排序，并按时间进行分组，每组取1/3；全表已删除记录取最近10天，按照浏览时间、浏览时长排序截取前1/3；
     * noDelateRecentDay:没删除最近取多少天
     * alreadyDelateRecentDay:已删除最近取多少天
     * noDelateRecentGetRate:没删除最近每天记录取几等分
     * alreadyDelateRecentGetRate:已删除最近每天记录取多少几等分
     * */
    @GetMapping("/recommend/{phone}/{noDelateRecentDay}/{alreadyDelateRecentDay}/{noDelateRecentGetRate}/{alreadyDelateRecentGetRate}")
    public RpsMsg consumerHomeRecommendCommodityId(@PathVariable long phone, @PathVariable int noDelateRecentDay, @PathVariable int alreadyDelateRecentDay, @PathVariable int noDelateRecentGetRate, @PathVariable int alreadyDelateRecentGetRate) {
        //得到未删除的时间以及条数
        List<Map<String, Object>> nodeleteDates = historyMapper.selectCountDateByRecentDay(phone, noDelateRecentDay, 0);
        //得到已删除的时间以及条数
        List<Map<String, Object>> readydeleteDates = historyMapper.selectCountDateByRecentDay(phone, alreadyDelateRecentDay, 1);
        if (nodeleteDates.size() == 0 && readydeleteDates.size() == 0) {
            return new RpsMsg().setStausCode(200).setData(nodeleteDates);
        }
        //合并集合,mysql同i条数用的是Long
        Map<Date, Integer> map = new HashMap<>();
        nodeleteDates.stream().peek(s -> {
            Date date = (Date) s.get("date");
            map.put(date, (int) Math.ceil((Long) s.get("num") / noDelateRecentGetRate));
        }).collect(Collectors.toList());
        readydeleteDates.stream().peek(s -> {
            Date date = (Date) s.get("date");
            map.put(date, (int) Math.ceil((Long) s.get("num") / alreadyDelateRecentGetRate));
        }).collect(Collectors.toList());
        //获取两者的商品id
        List<String> nodeleteCommodityIds = historyMapper.selectCommodityByDateAndNumMany(phone, map);
        //去重
        nodeleteCommodityIds = nodeleteCommodityIds.stream().distinct().collect(Collectors.toList());
        return new RpsMsg().setStausCode(200).setData(nodeleteCommodityIds);
    }


}
