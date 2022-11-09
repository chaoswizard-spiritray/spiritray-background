package spiritray.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Cpi;

/**
 * ClassName:CpiMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/6/19 15:42
 * @Author:灵@email
 */
@Mapper
@Repository
public interface CpiMapper {

    /*插入单条支付记录*/
    public int insertCpi(@Param("cpi") Cpi cpi);

    /*插入指定主订单号下所有的订单细节支付信息*/
    public int insertCpis(@Param("cpi") Cpi cpi);

    /*查询指定订单细节的支付信息*/
    public Cpi selectCpiByCpiId(@Param("cpiId") String cpiId);
}
