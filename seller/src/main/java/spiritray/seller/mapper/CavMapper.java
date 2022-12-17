package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Attribute;
import spiritray.common.pojo.PO.Category;
import spiritray.common.pojo.PO.Cav;

import java.util.List;

/**
 * ClassName:CavMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/4/26 13:42
 * @Author:灵@email
 */
@Repository
public interface CavMapper {
    /*删除指定商品id的所有属性*/
    public int deleteAllCavByCommodityId(@Param("commodityId") String commodityId);

    /*通过种类id查询商品种类信息*/
    public List<Category> selectCategoryById(int id);

    /*批量插入属性*/
    public int insertAttributes(@Param("categoryId") long categoryId, @Param("attributes") List<Attribute> attributes);

    /*通过种类id查询其属性*/
    public List<Attribute> selectAttributeByCategoryId(int categoryId);

    /*添加cav*/
    public int insertCavS(@Param("cavs") List<Cav> cavs);

    /*通过商品id查询多值cav*/
    public List<SSMap> selectCavByCommodityId(@Param("commodityId") String commodityId, @Param("isMul") int isMul);

    /*通过商品id查询其所有cav*/
    public List<Cav> selectAllCavCommodityId(@Param("commodityId") String commodityId);
}
