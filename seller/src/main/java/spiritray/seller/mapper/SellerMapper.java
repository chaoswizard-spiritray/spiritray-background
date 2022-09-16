package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Seller;

/**
 * ClassName:SellerMapper
 * Package:spiritray.consumer.mapper
 * Description:
 *
 * @Date:2022/4/17 16:44
 * @Author:灵@email
 */
@Mapper
@Repository
public interface SellerMapper {
    public Long selectPhoneByPhone(@Param("phone") long phone);

    public int insertSeller(@Param("seller") Seller seller);
}
