package spiritray.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Pts;

/**
 * ClassName:PtsMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/11/24 14:22
 * @Author:灵@email
 */
@Mapper
@Repository
public interface PtsMapper {
    public Pts selectPtsByOrderNumber(@Param("orderNumber") String orderNumber);
}
