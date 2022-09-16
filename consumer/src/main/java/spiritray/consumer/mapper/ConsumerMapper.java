package spiritray.consumer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.Consumer;

import java.util.List;

/**
 * ClassName:ConsumerMapper
 * Package:spriritray.consumer.mapper
 * Description:
 *
 * @Date:2022/4/14 9:28
 * @Author:灵@email
 */
@Mapper
@Repository
public interface ConsumerMapper {
    /*查询买家信息通过买家*/
    public Consumer selectConsumerByConsumer(@Param("consumer") Consumer consumer);

    /*插入买家信息*/
    public int insertConsumer(@Param("consumer") Consumer consumer);

    /*更新买家信息*/
    public int updateConsumer(@Param("consumer") Consumer consumer);

    /*查询买家收货地址信息*/
    public List<Address> selectAddressByPhone(@Param("phone") long phone);

    /*添加买家收货地址*/
    public int insertAddress(@Param("address") Address address);

    /*修改买家收货地址*/
    public int updateAddressById(@Param("address") Address address);

    /*删除买家收货地址*/
    public int deleteAddressById(@Param("addressId") String addressId, @Param("phone") long phone);
}