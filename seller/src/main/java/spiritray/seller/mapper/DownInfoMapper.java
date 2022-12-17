package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.DownInfo;

import java.util.List;

/**
 * ClassName:DownInfoMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/12/13 21:01
 * @Author:灵@email
 */
@Mapper
@Repository
public interface DownInfoMapper {
    /*添加商品下架信息*/
    public int insertDownInfoOne(@Param("downInfo") DownInfo downInfo);

    /*删除指定商品下架信息*/
    public int deleteDownInfoManyByCommodityId(@Param("commodityIds") List<String> commoditys);
}
