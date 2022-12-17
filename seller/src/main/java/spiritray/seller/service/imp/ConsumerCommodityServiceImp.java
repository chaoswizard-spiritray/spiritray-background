package spiritray.seller.service.imp;

import cn.hutool.extra.tokenizer.TokenizerUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import org.apache.tomcat.util.buf.Utf8Decoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.DTO.CommodityShop;
import spiritray.common.pojo.DTO.HomeCommoditySimple;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Category;
import spiritray.seller.mapper.CategoryMapper;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.mapper.ConsumerCommodityMapper;
import spiritray.seller.service.ConsumerCommodityService;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
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

    @Autowired
    private RedisTemplate redisTemplate;

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
            data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByTypes(resultTypes, pageNum * recordNum, recordNum);
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
                data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByTypes(types, pageNum * recordNum, recordNum);
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
                data = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByTypes(types, pageNum * recordNum, recordNum);
            }

        }
        //判断商品的数目,如果已经查完了就按照好评率排序商品，随机选取一些数据返回
        if (data.size() < recordNum) {
            //判断查询出来的数目，假设结果已经查询
            //条数不够说明系统至少有pageNum*recordNum+recordNum条
            int newPage = ((pageNum * recordNum + recordNum) / (recordNum - data.size())) / 2;
            pageNum = ThreadLocalRandom.current().nextInt(newPage);
            List<HomeCommoditySimple> temp = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRate(pageNum * (recordNum - data.size()) * 2, (recordNum - data.size()) * 2);
            //将补充数据加入
            data.addAll(temp);
        }
        return new RpsMsg().setStausCode(200).setData(data);
    }

    @Override
    public RpsMsg queryConsumerCommodityDetail(String commodityId) {
        try {
            CommodityShop commodityShop = consumerCommodityMapper.selectCommodityShopByCommodityId(commodityId);
            if (commodityShop == null)
                return new RpsMsg().setStausCode(400).setMsg("商品已下架");
            else
                return new RpsMsg().setStausCode(200).setData(commodityShop);

        } catch (Exception e) {
            return new RpsMsg().setStausCode(400).setMsg("商品已下架");
        }
    }

    /*
     * 检索维度：商品名、商品种类、商品品牌(依次往后，检索范围逐步扩大，当前面的数据已经搜索完，后面的补充上)。基于内存排序以及分页
     * 商品名检索权重初始化策略：
     *   - 策略：分词字数越多，权重越大。每个分词的初始权重设置为词的长度。
     *   - 额外初始化增加策略：贪心策略，按照一般人说话的逻辑，往往会将重要的名词放在后面，前面会添加修饰词。
     *       所以我们这个策略是位置越后，额外增加的权重越多。初始额外权重为分词数组的索引下标。
     * 商品名检索权重计算策略:
     *   - 普通策略：将匹配到的分词权重进行累加即可
     *   - 额外累加策略：当商品名中匹配到的分词是商品种类、商品品牌时，额外增加5权重。
     *   前提条件：我们预先将当前售卖中的商品的种类（-一般作为不可再分）、名称、属性值、sku价格进行了分词，并将其导入到了自定义分词文件中，
     *   且将所有分词指明分词来源-种类、名称、属性值。。存储到数据库中的系统分词表
     * 参数说明：
     * String word：搜索词
     * int pageNo：页号
     * int pageNum：每页数目
     * Map params：额外参数、如上架时间、价格区间。按照价格、时间、好评率排序
     * */
    @Override
    public RpsMsg queryConsumerCommoditySearch(String word, int pageNo, int pageNum, Map params) {
        //加载自定义配置字典
        WordDictionary.getInstance().loadUserDict(Paths.get(new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + "/dict/commodity.dict").getAbsolutePath()), new Utf8Decoder().charset());
        //将用户输入分词
        List<String> list = new ArrayList<>();
        TokenizerUtil.createEngine().parse(word).forEach(s -> {
            list.add(s.getText());
        });
        //将分词去重
        list.stream().distinct().count();
        //然后拼接匹配模式
        StringBuilder regex = new StringBuilder("");//种类匹配模式
        list.stream().peek(s -> {
            regex.append(s).append('|');
        }).count();
        String regexTemp = regex.toString();
        if (regexTemp.lastIndexOf('|') == regexTemp.length() - 1) {
            regex.deleteCharAt(regexTemp.length() - 1);
        }
        //先匹配商品种类
        List<Integer> matchCategorys = categoryMapper.selectCategoryIdByToken(regex.toString());//得到匹配到的分词种类
        Set<Integer> resultCategorys = new HashSet<>();//最终需要查询的种类,因为可能存在父类和子类都含有分词，从而导致结果有重复，我们需要进行去重,所以使用集合
        //当种类能够匹配到分词时，统计出所有的应该加载的种类，避免单个字符触发种类匹配，将种类导入自定义的分词字典
        if (matchCategorys.size() > 0) {
            //循环获取子种类直到父种类队列为空
            while (matchCategorys.size() > 0) {
                //得到当前父种类下的子类
                List<Category> categories = categoryMapper.selectCategoryChildIdAndFatherIdByFatherId(matchCategorys);
                if (categories.size() == 0) {
                    //如果查询没有结果，将当前父种类完全加入，直接结束
                    resultCategorys.addAll(matchCategorys);
                    break;
                }
                //如果有结果过滤出已经没有子类的父类
                Set<Integer> set = new HashSet<>(matchCategorys);
                //因为查询结果的父id必然是数组的子集
                matchCategorys.clear();//清除父类
                matchCategorys.addAll(categories.stream().map(Category::getCategoryId).collect(Collectors.toSet()));//加入新的遍历集合
                set.removeAll(categories.stream().map(Category::getFather).collect(Collectors.toSet()));//过滤出结果集
                resultCategorys.addAll(set);//添加结果集
            }
        }
        //再检索分词中是否有品牌属性
        Set<String> brands = new HashSet<>(commodityMapper.selectCommodityBrandByBrands(list));
        //通过匹配模式匹配商品名，并获取商品名、商品id
        List<SSMap> commodityNameAndIds = consumerCommodityMapper.selectCommodityIdAndNameByTokens(regex.toString(), params);
        Map<String, Integer> commodityWeights = new HashMap<>();
        //计算权重
        Set<String> cateNames = null;
        if (!resultCategorys.isEmpty()) {
            cateNames = new HashSet<>(categoryMapper.selectCateNameByIds(new ArrayList<>(resultCategorys)));
        }
        String hotWord = computeCommodityNamesWeight(list, cateNames, brands.isEmpty() ? null : brands, commodityNameAndIds, commodityWeights);
        //统计搜索词
        if (pageNo == 0) {
            //如果是新产生搜索，就将搜索词加入统计
            addHotWord(hotWord);
        }
        //按权重降序
        List<Map.Entry<String, Integer>> resultCommoditys = orderCommodityByWeight(commodityWeights);
        //截取指定的商品数目
        List<Map.Entry<String, Integer>> tempCommoditys = spliceCommoditys(resultCommoditys, pageNo, pageNum);
        List<HomeCommoditySimple> commoditySimples = commoditySimples = null;
        if (tempCommoditys.size() > 0) {
            commoditySimples = consumerCommodityMapper.selectHomeCommoditySimpleOrderByfavorableRateByCommodityIds(tempCommoditys.stream().map(Map.Entry::getKey).collect(Collectors.toList()), params);
        }
        //判断是否截取到数据
        if (tempCommoditys.size() < pageNum) {
            //依次获取种类和品牌范围商品
            List<HomeCommoditySimple> tempHomeCommoditySimples = null;
            if (!resultCategorys.isEmpty()) {
                tempHomeCommoditySimples = consumerCommodityMapper.selectHomeCommoditySimpleTokenByParamsByTypes(new ArrayList<>(resultCategorys), params);
            }
            if (!brands.isEmpty()) {
                if (tempHomeCommoditySimples == null) {
                    tempHomeCommoditySimples = consumerCommodityMapper.selectHomeCommoditySimpleTokenByParamsByBrands(new ArrayList<>(brands), params);
                } else {
                    tempHomeCommoditySimples.addAll(consumerCommodityMapper.selectHomeCommoditySimpleTokenByParamsByBrands(new ArrayList<>(brands), params));
                }
            }
            if (tempHomeCommoditySimples != null) {
                if (commoditySimples == null) {
                    commoditySimples = tempHomeCommoditySimples;
                } else {
                    commoditySimples.addAll(tempHomeCommoditySimples);
                }
            }
        }
        //去重
        Set tempset = new HashSet();
        return new RpsMsg().setStausCode(200).setData((commoditySimples == null || commoditySimples.isEmpty()) ? null : commoditySimples.stream().filter(s -> {
            if (tempset.contains(s.getCommodityId())) {
                return false;
            } else {
                tempset.add(s.getCommodityId());
                return true;
            }
        }).collect(Collectors.toList()));
    }

    /*计算每个商品名的权重,并找寻最大匹配权重组合分词*/
    private String computeCommodityNamesWeight(List<String> tokens, Set<String> cates, Set<String> brands, List<SSMap> commodityNameAndIds, Map<String, Integer> weights) {
        //初始化分词权重
        List<Integer> tokenWeights = initTokenWeight(tokens);
        String maxMatchToken = null;
        int maxMatchWeight = 0;
        for (SSMap commodityNameAndId : commodityNameAndIds) {
            //匹配分词并计算权重
            int weight = 0;
            StringBuffer tempStringBuffer = new StringBuffer("");
            for (int i = 0; i < tokens.size(); i++) {
                if (commodityNameAndId.getAttributeValue().indexOf(tokens.get(i)) > -1) {
                    //如果匹配成功
                    weight += tokenWeights.get(i);
                    tempStringBuffer.append(tokens.get(i));
                    //再判断是否是种类名或者品牌，是就额外增加权重,因为
                    if (cates != null && cates.contains(tokens.get(i))) {
                        weight += 5;
                    }
                    if (brands != null && brands.contains(tokens.get(i))) {
                        weight += 5;
                    }
                }
            }
            //每次商品循环匹配完成后得到权重最大连续匹配分词组合，与全局最大权重连续匹配分词组合比较
            if (weight >= maxMatchWeight) {
                maxMatchWeight = weight;
                maxMatchToken = tempStringBuffer.toString();
            }
            //保存商品id及其权重
            weights.put(commodityNameAndId.getAttributeName(), weight);
        }
        //最后返回的就是我们本次搜索词中提取的得到的热词
        return maxMatchToken;
    }

    /*初始化分词权重*/
    private List<Integer> initTokenWeight(List<String> tokens) {
        List<Integer> tokenWeights = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            tokenWeights.add(tokens.get(i).length() + i);
        }
        return tokenWeights;
    }

    /*将商品权重进行降序排列*/
    private List orderCommodityByWeight(Map<String, Integer> weights) {
        //将组合分词按照权重降序
        return new ArrayList<>(weights.entrySet()).stream().sorted(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if (o1.getValue() > o2.getValue()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }).collect(Collectors.toList());
    }

    //截取指定商品
    private List<Map.Entry<String, Integer>> spliceCommoditys(List<Map.Entry<String, Integer>> commoditys,
                                                              int pageNo, int pageNum) {
        return commoditys.stream().skip(pageNo * pageNum).limit(pageNum).collect(Collectors.toList());
    }

    /*添加热词*/
    private void addHotWord(String hotWord) {
        String hotWordSet = "hotWordSet";
        redisTemplate.opsForZSet().incrementScore(hotWordSet, hotWord, 1);
    }
}

