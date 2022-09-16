package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.BO.CheckOrderInfo;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Sku;

import java.util.List;

/**
 * ClassName:SkuMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/5/24 9:48
 * @Author:灵@email
 */
@Mapper
@Repository
public interface SkuMapper {
    /*添加sku数组*/
    public int insertSkus(@Param("skus") List<Sku> skus);

    /*查询sku根据商品id*/
    public List<Sku> selectSkuByCommodityId(@Param("commodityId") String commodityId);

    /*查询商品的sku数量*/
    public Integer selectSkuNumByCommodityAndSku(@Param("commodityId") String commodityId, @Param("skuValue") String skuValue);

    /*根据商品id和sku查询订单商品检测信息*/
    public CheckOrderInfo selectCheckOrderInfoByCommodityId(@Param("ssMap") SSMap ssMap);

    /*修改sku数量*/
    public int updateSkuNumByCommodityAndSku(@Param("commodityId") String commodityId, @Param("skuValue") String skuValue, @Param("num") int num, @Param("isSub") int isSub);
}
