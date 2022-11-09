package spiritray.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.NNMap;
import spiritray.common.pojo.DTO.SNMap;

import java.util.List;
import java.util.Map;

/**
 * ClassName:OrderCountMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/6/14 19:38
 * @Author:灵@email
 */
@Mapper
@Repository
public interface OrderCountMapper {

    /*统计指定店铺所有商品本月销售数目*/
    public List<SNMap> selectAllCommoditySellCountByStoreId(@Param("storeId") String storeId);

    /*统计指定买家的的各类订单数目*/
    public List<NNMap> selectConsumerOrderNum(@Param("phone") Long phone);
}
