package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.CheckInfoExtend;
import spiritray.common.pojo.PO.CheckInfo;

/**
 * ClassName:CheckInfoMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/6/14 0:15
 * @Author:灵@email
 */
@Mapper
@Repository
public interface CheckInfoMapper {
    /*插入指定商品默认检查信息*/
    public int insertCheckInfo(@Param("commodityId") String commodityId);

    /*修改商品检测信息*/
    public int updateCheckInfo(@Param("checkinfo") CheckInfo checkInfo);

    /*删除商品检测信息*/
    public int deleteCheckInfo(@Param("commodityId") String commodityId);

    /*查询指定商品扩展检查信息*/
    public CheckInfoExtend selectCheckInfoExtend(@Param("commodityId") String commodityId);
}
