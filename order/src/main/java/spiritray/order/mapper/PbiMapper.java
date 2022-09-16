package spiritray.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Pbi;

/**
 * ClassName:PbiMapper
 * Package:spiritray.order.mapper
 * Description:
 *
 * @Date:2022/6/19 15:56
 * @Author:灵@email
 */
@Mapper
@Repository
public interface PbiMapper {

    /*插入退款信息*/
    public int insertPbi(@Param("pbi") Pbi pbi);
}
