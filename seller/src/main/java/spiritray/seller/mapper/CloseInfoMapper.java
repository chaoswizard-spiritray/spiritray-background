package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.CloseStore;

/**
 * ClassName:CloseInfoMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/12/11 18:45
 * @Author:灵@email
 */
@Mapper
@Repository
public interface CloseInfoMapper {
    /*查询指定店铺查封信息*/
    public CloseStore selectCloseInfoByStoreId(@Param("storeId") String storeId);

    /*添加店铺查封信息*/


}
