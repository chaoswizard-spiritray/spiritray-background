package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * ClassName:CommodityDetailMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/12/12 17:37
 * @Author:灵@email
 */
@Mapper
@Repository
public interface CommodityDetailMapper {
    /*查询指定商品的详情*/
    public String getCommodityDetail(@Param("commodityId") String commodityId);

    /*更新指定商品详情*/
    public int updateCommodityDetail(@Param("commodityId") String commodityId, @Param("detail") String detail);
}
