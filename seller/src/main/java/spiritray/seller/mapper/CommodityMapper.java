package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.*;
import spiritray.common.pojo.PO.Commodity;

import java.util.List;

/**
 * ClassName:CommodityMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/4/26 21:16
 * @Author:灵@email
 */
@Repository
public interface CommodityMapper {
    /*查询所有商品id*/
    public List<String> selectAllCommodityId();

    /*查询指定商品id的商品信息*/
    public Commodity selectCommodityById(@Param("commodityId") String commodityId);

    /*查询批量商品的名字*/
    public List<SSMap> selectCommodityName(@Param("ids") List ids);

    /*插入商品基本信息*/
    public int insertCommodity(@Param("commodity") Commodity commodity);

    /*修改指定商品状态*/
    public int updateCommodityState(@Param("state") int state, @Param("commodityId") String commodity);

    /*查询商家已上架商品简单信息*/
    public List<InSellSimple> selectInSellSimpleByStoreId(@Param("storeId") String storeId);

    /*查询已上架商品详细信息*/
    public List<InSellDetail> selectInSellDetailByCommodityId(@Param("commodityId") String commodity);

    /*查询待审核商品的简洁信息*/
    public List<InCheckSimple> selectInCheckSimpleByStoreId(@Param("storeId") String storeId);

    /*查询已下架商品简洁信息*/
    public List<NoSellSimple> selectNoSellSimpleByStoreId(@Param("storeId") String storeId);

    /*查询买家端商品简洁信息*/
    public List<CommodityConsumerSimple> selectCommodityConsumerSimple(@Param("condition") CommodityCondition condition);

    /*查询指定商品审核详细信息*/
    public InCheckDetail selectInCheckDetailByCommodityId(@Param("storeId") String storeId, @Param("commodityId") String commodityId);

    /*查询简略商品信息通过审核状态*/
    public List<CommoditySimple> selectCommoditySimpleByCheckState(@Param("state") int state);
}
