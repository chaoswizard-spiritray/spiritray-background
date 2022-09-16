package spiritray.seller.service.imp;

import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.DTO.HomeCommoditySimple;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.seller.mapper.ConsumerCommodityMapper;
import spiritray.seller.service.ConsumerCommodityService;

import java.util.List;

/**
 * ClassName:ConsumerCommodityServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 *
 * @Date:2022/6/15 9:22
 * @Author:灵@email
 */
@Service
public class ConsumerCommodityServiceImp implements ConsumerCommodityService {
    @Autowired
    private ConsumerCommodityMapper consumerCommodityMapper;

    @Autowired
    private RestTemplate restTemplate;

    private String ORDER_URL = "http://localhost:8082";

    private String CONSUMER_URL = "http://localhost:8080";

    @Override
    public RpsMsg queryHomeCommodity(int pageNum, int recordNum, long phone) {
        if (phone < 0) {
            //如果没有登录，统计商品好评率，然后进行排序，按照分页进行返回
            return new RpsMsg().setStausCode(200).setData(consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRate(pageNum, recordNum));
        } else {
            //如果登录了就获取其商品收藏按时间排序前十条信息，并且获取浏览历史按浏览时长和浏览起始时间前十条信息
            List<String> collections = (List<String>) restTemplate.getForObject(CONSUMER_URL + "/collection/plat/" + phone, RpsMsg.class).getData();
            List<String> his = (List<String>) restTemplate.getForObject(CONSUMER_URL + "/history/plat/" + phone + "/10", RpsMsg.class).getData();
            if (collections.size() == 0 && his.size() == 0) {
                return new RpsMsg().setStausCode(200).setData(consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRate(pageNum, recordNum));
            } else {
                if (his.size() == 0) {
                    his = collections;
                } else if (collections.size() == 0) {
                    //去重并连接数组
                    List<String> finalHis = his;
                    collections.forEach((item) -> {
                        finalHis.forEach((hi) -> {
                            if (hi.equals(item)) {
                                collections.remove(item);
                            } else {
                                finalHis.add(item);
                            }
                        });
                    });
                    his = finalHis;
                }
                //确定这些商品的种类并获取其信息
                List<HomeCommoditySimple> data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByCommodityIds(his, pageNum, recordNum);
                //判断商品的数目,如果已经查完了就按照好评率排序随机选取一些数据返回
                if (data.size() < recordNum) {
                    if (pageNum != 0) {
                        pageNum = RandomUtil.randomInt(0, pageNum);
                    }
                    List<HomeCommoditySimple> temp = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRate(recordNum - data.size(), pageNum);
                    temp.forEach((item) -> {
                        data.add(item);
                    });
                }
                return new RpsMsg().setStausCode(200).setData(data);
            }
        }
    }

    @Override
    public RpsMsg queryConsumerCommodityDetail(String commodityId) {
        return new RpsMsg().setData(consumerCommodityMapper.selectCommodityShopByCommodityId(commodityId)).setStausCode(200);
    }
}
