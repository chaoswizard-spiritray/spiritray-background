package spiritray.plant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * ClassName:StaffMapper
 * Package:spiritray.plant.mapper
 * Description:
 *
 * @Date:2022/6/13 8:42
 * @Author:灵@email
 */
@Mapper
@Repository
public interface StaffMapper {

    /*根据工号查询邮箱*/
    public String selectMailByStaffId(@Param("staffId")long staffId);
}
