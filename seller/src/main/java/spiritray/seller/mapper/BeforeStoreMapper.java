package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.HomeCommoditySimple;
import spiritray.common.pojo.PO.Category;
import spiritray.common.pojo.PO.Commodity;
import spiritray.common.pojo.PO.Store;

import java.util.List;

/**
 * ClassName:BrforeStoreMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/12/11 13:52
 * @Author:灵@email
 */
@Mapper
@Repository
public interface BeforeStoreMapper {
    /*查询指定店铺中的在售的商品id*/
    public List<String> selectInSellCommodityIdsByStoreId(@Param("storeId") String storeId);

    /*将指定店铺中的在售商品下架*/
    public int updateDownCommodityByStoreId(@Param("storeId") String storeId);

    /*获取到店铺下已下架的商品id*/
    public List<Commodity> selectAllNoSellCommodityIdByStoreId(@Param("storeId") String storeId);

    /*删除指定店铺下的所有已下架的商品*/
    public int deleteAllNoSellCommodityByStoreId(@Param("storeId") String storeId);

    /*查询指定店铺是否有在审核中的商品*/
    public int selectCountInCheckCommodity(@Param("storeId") String storeId);

    /*查询所有商品信息*/
    public List<HomeCommoditySimple> selectAllCommoditysByStorejId(@Param("storeId") String storeId);

    /*查询最近7天上架商品*/
    public List<HomeCommoditySimple> selectRecentCommoditysByStorejId(@Param("storeId") String storeId);

    /*查询店铺的商品种类信息*/
    public List<Category> selectStoreAllCategory(@Param("storeId") String storeId);

    /*查询店铺的商品信息指定种类*/
    public List<HomeCommoditySimple> selectAllCommoditysByStoreIdByCateId(@Param("storeId") String storeId, @Param("cateId") Long cateId);

    /*查询指定店铺商家关闭信息*/
    public List<Store> selectSellerCloseInfo(@Param("storeId") String storeId);
}
