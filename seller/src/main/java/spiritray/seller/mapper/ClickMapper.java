package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Click;

/**
 * ClassName:ClickMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/6/19 21:23
 * @Author:灵@email
 */
@Mapper
@Repository
public interface ClickMapper {

    /*查询商品是否存在*/
    public String selectCommodityIdByCommodityId(String commodityId);

    /*插入点击信息*/
    public int insertClick(@Param("click") Click click);

}
