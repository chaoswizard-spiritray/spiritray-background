package spiritray.consumer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.CommodityCollection;

import java.util.List;

/**
 * ClassName:CollectionMapper
 * Package:spiritray.consumer.mapper
 * Description:
 *
 * @Date:2022/6/15 14:44
 * @Author:灵@email
 */
@Mapper
@Repository
public interface CollectionMapper {
    /*查询指定用户所有收藏信息*/
    public List<CommodityCollection> selectAllCollection(@Param("phone") long phone);

    /*查询指定用户收藏商品id*/
    public List<String> selectCollectionCommodityIdByPhone(@Param("phone") long phone);

    /*查询指定用户是否收藏了指定商品*/
    public String selectIsCollectionByPhoneAndCommodityId(@Param("phone") long phone, @Param("commodityId") String commodityId);

    /*添加收藏*/
    public int insertCollectionOne(@Param("collection") CommodityCollection collection);

    /*删除收藏*/
    public int deleteCollectionOne(@Param("collection") CommodityCollection collection);
}
