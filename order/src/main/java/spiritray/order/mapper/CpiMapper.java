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

    /*插入支付记录*/
    public int insertCpi(@Param("cpi") Cpi cpi);
}
