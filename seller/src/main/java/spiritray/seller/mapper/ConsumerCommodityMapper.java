package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.CommodityShop;
import spiritray.common.pojo.DTO.HomeCommoditySimple;
import spiritray.common.pojo.DTO.SNMap;
import spiritray.common.pojo.DTO.SSMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName:ConsumerCommodityMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/6/15 10:53
 * @Author:灵@email
 */
@Mapper
@Repository
public interface ConsumerCommodityMapper {
    /*查询分词种类下的搜索商品*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleTokenByParamsByBrands(@Param("brands") List<String> brands, @Param("params") Map params);

    /*查询分词种类下的搜索商品*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleTokenByParamsByTypes(@Param("types") List<Integer> types, @Param("params") Map params);

    /*根据分词、种类、品牌查询分词商品价格最大值*/
    public Float selectTokenFliterDataPriceMax(@Param("regexp") String regexp, @Param("cates") Set cates, @Param("brands") Set brands);

    /*根据分词、种类、品牌查询分词商品价格最小值*/
    public Float selectTokenFliterDataPriceMin(@Param("regexp") String regexp, @Param("cates") Set cates, @Param("brands") Set brands);

    /*根据分词、种类、品牌查询分词商品发货地址*/
    public List<String> selectTokenFliterxDataAddress(@Param("regexp") String regexp, @Param("cates") Set cates, @Param("brands") Set brands);

    /*根据分词、种类、品牌查询分词商品种类*/
    public List<SNMap> selectTokenFliterDataCates(@Param("regexp") String regexp, @Param("cates") Set cates, @Param("brands") Set brands);

    /*根据分词、种类、品牌查询分词商品品牌*/
    public List<String> selectTokenFliterDataBrands(@Param("regexp") String regexp, @Param("cates") Set cates, @Param("brands") Set brands);

    /*查询分词字段*/
    public List<String> selectTokenCol();

    /*根据商品id查询*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRateByCommodityIds(@Param("ids") List<String> ids, @Param("params") Map params);

    /*根据分词以及附加条件获取商品id和商品名*/
    public List<SSMap> selectCommodityIdAndNameByTokens(@Param("regexp") String regexp, @Param("params") Map params);

    /*查询指定种类范围内的首页数据*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRateByTypes(@Param("types") List<Integer> types, @Param("pageNum") int pageNum, @Param("recordNum") int recordNum);

    /*查询指定范围内的品牌首页商品数据*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRateByBrands(@Param("brands") List<String> brands, @Param("pageNum") int pageNum, @Param("recordNum") int recordNum);

    /*统计商品好评率并按照好评率排序*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRate(@Param("pageNum") int pageNum, @Param("recordNum") int recordNum);

    /*查询客户端商品详细信息通过id*/
    public CommodityShop selectCommodityShopByCommodityId(@Param("commodityId") String commodityIds);

    /*根据分词查找信息*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByTokenWord(@Param("word") String word);

}
