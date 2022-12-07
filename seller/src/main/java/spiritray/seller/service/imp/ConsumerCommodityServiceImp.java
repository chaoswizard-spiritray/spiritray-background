package spiritray.seller.service.imp;

import cn.hutool.extra.tokenizer.TokenizerUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import lombok.SneakyThrows;
import org.apache.tomcat.util.buf.Utf8Decoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.DTO.HomeCommoditySimple;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Category;
import spiritray.seller.mapper.CategoryMapper;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.mapper.ConsumerCommodityMapper;
import spiritray.seller.service.ConsumerCommodityService;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * ClassName:ConsumerCommodityServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 * https://blog.csdn.net/weixin_45764765/article/details/121510052
 * https://blog.51cto.com/vipstone/5408732
 *
 * @Date:2022/6/15 9:22
 * @Author:灵@email
 */
@Service
public class ConsumerCommodityServiceImp implements ConsumerCommodityService {
    @Autowired
    private ConsumerCommodityMapper consumerCommodityMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    private RestTemplate restTemplate;

    private JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

    private String ORDER_URL = "http://localhost:8082";

    private String CONSUMER_URL = "http://localhost:8080";

    /*
     * 算法逻辑细节
     *   登录者：
     *       浏览记录：全表未删除记录取最近10天的浏览记录，按照浏览时间、浏览时长排序，并按时间进行分组，每组取1/3；全表已删除记录取最近10天，按照浏览时间、浏览时长排序截取前1/3；
     *       商品收藏：获取所有的商品收藏
     *       下单商品：也是获取所有
     *       然后将前三者取商品id的并集，然后查询商品所属种类，获取到的种类去重，看种类数量是否大于5，如果大于就直接使用该种类集合进行查询，
     *       查询结果按照好评率进行排序，好评率是根据商品评论星级>=3判定。
     *
     *       平台商品种类： 如果种类不大于5，就从平台售卖中的商品中获取到商品种类,并且从中随机选取种类，不能是已有的，补充到推荐种类数量到5，
     *                       如果还是不足，就使用这个种类集即可。
     *   如果没有登录：
     *       就只执行第4部分，随机选取5个种类并根据好评率查询。
     *
     * */
    @Override
    public RpsMsg queryHomeCommodity(int pageNum, int recordNum, long phone) {
        int checkTypeNum = 5;//选取的商品种类上界
        List<HomeCommoditySimple> data = null;//查询结果
        if (phone < 0) {
            //如果没有登录，统计商品好评率，然后进行排序，按照分页进行返回
            List<Integer> allTypes = commodityMapper.selectCommodityCateIdByCommoditys(null);
            List<Integer> resultTypes = new ArrayList<>();
            if (allTypes.size() <= checkTypeNum) {
                resultTypes = allTypes;
            } else {
                //随机选取并加入
                for (int i = 0; i < checkTypeNum; i++) {
                    int index = ThreadLocalRandom.current().nextInt(allTypes.size());
                    resultTypes.add(allTypes.get(index));
                    allTypes.remove(index);
                }
            }
            data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByTypes(resultTypes, pageNum*recordNum, recordNum);
        } else {
            //得到浏览商品id
            List<String> hisCommodityIds = (List<String>) restTemplate.getForObject(CONSUMER_URL + "/history/recommend/" + phone + "/10/10/3/3", RpsMsg.class).getData();
            //得到收藏商品
            List<String> collections = (List<String>) restTemplate.getForObject(CONSUMER_URL + "/collection/plat/" + phone, RpsMsg.class).getData();
            //得到下单商品
            List<String> orders = (List<String>) restTemplate.getForObject(ORDER_URL + "/order/commodityId/" + phone, RpsMsg.class).getData();
            //合并三者并去重
            hisCommodityIds.addAll(collections);
            hisCommodityIds.addAll(orders);
            List<String> result = hisCommodityIds.stream().distinct().collect(Collectors.toList());
            //得到商品种类
            List<Integer> types = new ArrayList<>();
            if (result != null && result.size() > 0) {
                types = commodityMapper.selectCommodityCateIdByCommoditys(result);
            }
            //判断长度
            if (types.size() >= checkTypeNum) {
                data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByTypes(types, pageNum*recordNum, recordNum);
            } else {
                //如果小于，就补充
                List<Integer> allTypes = commodityMapper.selectCommodityCateIdByCommoditys(null);
                //如果大集合以及不足
                if (allTypes.size() <= checkTypeNum) {
                    types = allTypes;
                } else {
                    //得到差集
                    Set set1 = new HashSet();
                    set1.addAll(types);
                    Set set2 = new HashSet();
                    set2.addAll(allTypes);
                    //因为set1是set2的真子集
                    set2.removeAll(set1);
                    List<Integer> diffreences = new ArrayList<>(set2);
                    //判断长度
                    int less = checkTypeNum - types.size();
                    //随机选取并加入
                    for (int i = 0; i < less; i++) {
                        int index = ThreadLocalRandom.current().nextInt(diffreences.size());
                        types.add(diffreences.get(index));
                        diffreences.remove(index);
                    }
                }
                //使用最终的种类查询商品
                data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByTypes(types, pageNum*recordNum, recordNum);
            }

        }
        //判断商品的数目,如果已经查完了就按照好评率排序商品，随机选取一些数据返回
        if (data.size() < recordNum) {
            //判断查询出来的数目，假设结果已经查询
            //条数不够说明系统至少有pageNum*recordNum+recordNum条
            int newPage = ((pageNum * recordNum + recordNum) / (recordNum - data.size()))/2;
            pageNum = ThreadLocalRandom.current().nextInt(newPage);
            List<HomeCommoditySimple> temp = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRate(pageNum*(recordNum - data.size())*2, (recordNum - data.size())*2);
            //将补充数据加入
            data.addAll(temp);
        }
        return new RpsMsg().setStausCode(200).setData(data);
    }

    @Override
    public RpsMsg queryConsumerCommodityDetail(String commodityId) {
        return new RpsMsg().setData(consumerCommodityMapper.selectCommodityShopByCommodityId(commodityId)).setStausCode(200);
    }

    @Override
    public RpsMsg queryConsumerCommoditySearch(String word) {
        //解析搜索词，人们再搜索商品时，搜索词往往是包含某一品牌、种类、属性值、不确定的商品名，其他可能是价格或者是乱输入的符号。
        //优先级如下：种类、商品名、商品属性值。-----注意：每个分词是整个分词系统中的不可再分的字符串。
        /*具体逻辑如下：
         *   前提条件：我们预先将当前售卖中的商品的种类（-一般作为不可再分）、名称、属性值、sku价格进行了分词，并将其导入到了自定义分词文件中，
         *   且将所有分词指明分词来源-种类、名称、属性值。。存储到数据库中的系统分词表
         *
         *   查询逻辑：
         *   1、先按照自定义分词表对查询内容进行分词，得到一个查询集合。
         *   2、将这个查询集合与数据库中的分词表进行比对，剔除在数据库不存在的分词。并得到剩下分词位于商品来源
         *   3、将分词按照来源分组，并按照长度进行排序。
         *   4、将对应来源的分词与对应字段进行匹配得到查询结果
         *
         * */
            //加载自定义配置字典
            WordDictionary.getInstance().loadUserDict(Paths.get(new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + "/dict/commodity.dict").getAbsolutePath()), new Utf8Decoder().charset());
            //将用户输入分词
            List<String> list = new ArrayList<>();
            StringBuilder regex = new StringBuilder("");//匹配串
            TokenizerUtil.createEngine().parse(word).forEach(s -> {
                list.add(s.getText());
                regex.append(s.getText()).append('|');
            });
            String regexTemp = regex.toString();
            if (regexTemp.lastIndexOf('|') == regexTemp.length() - 1) {
                regex.deleteCharAt(regexTemp.length() - 1);
            }
            //先匹配商品种类
            System.out.println(list);
            System.out.println(regex.toString());
            List<Integer> matchCategorys = categoryMapper.selectCategoryIdByToken(regex.toString());//得到匹配到的分词种类
            List<Integer> resultCategorys = new ArrayList<>();//最终需要查询的种类
            //当种类能够匹配到分词时，统计出所有的应该加载的种类，避免单个字符触发种类匹配，将种类导入自定义的分词字典
            if (matchCategorys.size() > 0) {
                //循环获取子种类直到父种类队列为空
                while (matchCategorys.size() == 0) {
                    //得到当前父种类下的子类
                    List<Category> categories = categoryMapper.selectCategoryChildIdAndFatherIdByFatherId(matchCategorys);
                    if (categories.size() == 0) {
                        //如果查询没有结果，将当前父种类完全加入，直接结束
                        resultCategorys.addAll(matchCategorys);
                        break;
                    }
                    //如果有结果过滤出已经没有子类的父类
                    Set<Integer> set = new HashSet<>(matchCategorys);
                    matchCategorys.clear();//清空原数组
                    //因为查询结果的父id必然是数组的子集
                    matchCategorys.clear();//清除父类
                    matchCategorys.addAll(categories.stream().map(Category::getCategoryId).collect(Collectors.toSet()));//加入新的遍历集合
                    set.removeAll(categories.stream().map(Category::getFather).collect(Collectors.toSet()));//过滤出结果集
                    resultCategorys.addAll(set);//添加结果集
                }
            }
            //匹配商品
            return new RpsMsg().setData(matchCategorys);
        }
    }
