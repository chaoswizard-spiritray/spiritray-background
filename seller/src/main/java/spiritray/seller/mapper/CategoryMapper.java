package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Category;

import java.util.List;

/**
 * ClassName:CategoryMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/12/4 17:37
 * @Author:灵@email
 */
@Mapper
@Repository
public interface CategoryMapper {
    /*根据分词获取到种类id*/
    public List<Integer> selectCategoryIdByToken(@Param("regex") String regex);

    /*查询指定id下的所有子id以及对应的父id*/
    public List<Category> selectCategoryChildIdAndFatherIdByFatherId(@Param("ids") List<Integer> ids);

    /*插入指定种类*/
    public Integer insertCategory(@Param("cate") Category category);
}
