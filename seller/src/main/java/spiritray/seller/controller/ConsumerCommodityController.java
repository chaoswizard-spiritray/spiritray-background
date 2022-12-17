package spiritray.seller.controller;

import cn.hutool.extra.tokenizer.TokenizerUtil;
import com.alibaba.fastjson.JSON;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import com.mysql.jdbc.log.Slf4JLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.Utf8Decoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SNMap;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Category;
import spiritray.common.pojo.PO.Store;
import spiritray.seller.mapper.CategoryMapper;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.mapper.ConsumerCommodityMapper;
import spiritray.seller.mapper.StoreMapper;
import spiritray.seller.service.ConsumerCommodityService;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName:ConsumerCommodityController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/6/15 9:19
 * @Author:灵@email
 */
@RestController
@RequestMapping("/commodity/consumer")
public class ConsumerCommodityController {
    @Autowired
    private ConsumerCommodityService consumerCommodityService;

    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    private ConsumerCommodityMapper consumerCommodityMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private JiebaSegmenter jiebaSegmenter = new JiebaSegmenter();

    /*查询首页商品信息*/
    @GetMapping("/home/{pageNum}/{recordNum}")
    public RpsMsg getHomeCommidty(@PathVariable int pageNum, @PathVariable int recordNum, HttpSession session) {
        //已经登录、和未登录是两种状况
        Object l = session.getAttribute("phone");
        if (l != null) {
            return consumerCommodityService.queryHomeCommodity(pageNum, recordNum, (Long) l);
        } else {
            return consumerCommodityService.queryHomeCommodity(pageNum, recordNum, -1);
        }
    }

    /*查询客户端商品详情展示信息*/
    @GetMapping("/info/detail/{commodityId}")
    public RpsMsg getComsumerCommodity(@PathVariable String commodityId) {
        return consumerCommodityService.queryConsumerCommodityDetail(commodityId);
    }

    /*查询搜索店铺*/
    @GetMapping("/search/store/{word}")
    public RpsMsg getConsumerStoreSearch(@PathVariable String word) {
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
        List<SSMap> tempList = storeMapper.selectStoreByRegexp(regex.toString());
        if (tempList.size() == 0) {
            return new RpsMsg().setStausCode(200);
        }
        //计算权重
        Map<String, Integer> weights = new HashMap<>();
        //计算权重
        computeCommodityNamesWeight(list, tempList, weights);
        //排序
        List<Store> stores = storeMapper.selectTokenStoreByIds(new HashSet<>(orderCommodityByWeight(weights)));
        return new RpsMsg().setStausCode(200).setData(stores);
    }

    /*查询搜索商品*/
    @GetMapping("/search/{word}/{pageNo}/{pageNum}")
    public RpsMsg getComsumerCommoditySearch(@PathVariable String word, String param, @PathVariable Integer pageNo, @PathVariable Integer pageNum) {
        List<Map> params;
        Map map = new HashMap();
        if (param != null) {
            //解析数据
            params = JSON.parseArray(param).toJavaList(Map.class);
            //转换为map便于取数据
            params.stream().peek(s -> map.putAll(s)).count();
        }
        return consumerCommodityService.queryConsumerCommoditySearch(word, pageNo, pageNum, map.size() == 0 ? null : map);
    }

    /*查询搜索商品的筛选数据集合
     * 价格区间
     * 发货地址
     * 商品种类
     * 商品品牌
     * */
    @GetMapping("/search/data/fliter/{word}")
    public RpsMsg getConsumerCommoditySearchFilterData(@PathVariable String word) {
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
                System.out.println("");
            }
        }
        //再检索分词中是否有品牌属性
        Set<String> brands = new HashSet<>(commodityMapper.selectCommodityBrandByBrands(list));
        //查询筛选数据
        Float maxPrice = consumerCommodityMapper.selectTokenFliterDataPriceMax(regex.toString(), resultCategorys.isEmpty() ? null : resultCategorys, brands.isEmpty() ? null : brands);
        Float minPrice = consumerCommodityMapper.selectTokenFliterDataPriceMin(regex.toString(), resultCategorys.isEmpty() ? null : resultCategorys, brands.isEmpty() ? null : brands);
        List<String> addresses = consumerCommodityMapper.selectTokenFliterxDataAddress(regex.toString(), resultCategorys.isEmpty() ? null : resultCategorys, brands.isEmpty() ? null : brands);
        List<SNMap> cates = consumerCommodityMapper.selectTokenFliterDataCates(regex.toString(), resultCategorys.isEmpty() ? null : resultCategorys, brands.isEmpty() ? null : brands);
        List<String> tempBrans = consumerCommodityMapper.selectTokenFliterDataBrands(regex.toString(), resultCategorys.isEmpty() ? null : resultCategorys, brands.isEmpty() ? null : brands);
        //封装数据
        Map<String, Object> result = new HashMap<>();
        List<Float> prices = new ArrayList<>();
        prices.add(minPrice);
        prices.add((float) (Math.floor(maxPrice + minPrice) / 2));
        prices.add(maxPrice);
        result.put("commodityPrice", prices);
        result.put("shipAddresses", addresses);
        result.put("commodityCates", cates);
        result.put("commodityBrands", tempBrans);
        return new RpsMsg().setStausCode(200).setData(result);
    }

    /*计算每个商品名的权重,并找寻最大匹配权重组合分词*/
    private void computeCommodityNamesWeight(List<String> tokens, List<SSMap> commodityNameAndIds, Map<String, Integer> weights) {
        //初始化分词权重
        List<Integer> tokenWeights = initTokenWeight(tokens);
        for (SSMap commodityNameAndId : commodityNameAndIds) {
            //匹配分词并计算权重
            int weight = 0;
            for (int i = 0; i < tokens.size(); i++) {
                if (commodityNameAndId.getAttributeValue().indexOf(tokens.get(i)) > -1) {
                    //如果匹配成功
                    weight += tokenWeights.get(i);
                }
            }
            //保存商品id及其权重
            weights.put(commodityNameAndId.getAttributeName(), weight);
        }
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
    private List<String> orderCommodityByWeight(Map<String, Integer> weights) {
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
        }).map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
