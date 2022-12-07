package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.CommodityShop;
import spiritray.common.pojo.DTO.HomeCommoditySimple;

import java.util.List;

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
    /*查询分词字段*/
    public List<String> selectTokenCol();

    /*查询指定种类范围内的首页数据*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRateByTypes(@Param("types") List<Integer> types, @Param("pageNum") int pageNum, @Param("recordNum") int recordNum);

    /*统计商品好评率并按照好评率排序*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRate(@Param("pageNum") int pageNum, @Param("recordNum") int recordNum);

    /*查询一定种类范围内首页数据*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByfavorableRateByCommodityIds(@Param("commodityIds") List<String> commodityIds, @Param("pageNum") int pageNum, @Param("recordNum") int recordNum);

    /*查询客户端商品详细信息通过id*/
    public CommodityShop selectCommodityShopByCommodityId(@Param("commodityId") String commodityIds);

    /*根据分词查找信息*/
    public List<HomeCommoditySimple> selectHomeCommoditySimpleOrderByTokenWord(@Param("word") String word);

}
