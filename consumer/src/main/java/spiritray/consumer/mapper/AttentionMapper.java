package spiritray.consumer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Attention;

import java.util.List;

/**
 * ClassName:AttentionMapper
 * Package:spiritray.consumer.mapper
 * Description:
 *
 * @Date:2022/12/11 11:26
 * @Author:灵@email
 */
@Mapper
@Repository
public interface AttentionMapper {
    /*查询指定用户关注的指定店铺*/
    public Attention selectAttentionByConsumerPhoneAndStoreId(@Param("storeId") String storeId, @Param("phone") Long phone);

    /*添加关注*/
    public int insertAttention(@Param("attention") Attention attention);

    /*删除关注记录*/
    public int deleteAttentionByConsumerPhoneAndStoreId(@Param("storeId") String storeId, @Param("phone") Long phone);

    /*获取用户所有的关注记录*/
    public List<Attention> selectConsumerAllAttention(@Param("phone") Long phone);

    /*统计指定店铺关注数目*/
    public Long selectStoreAttentionNum(@Param("storeId") String storeId);

    /*查询指定店铺的所有关注信息*/
    public List<Attention> selectStoreAllAttention(@Param("storeId") String storeId);
}
