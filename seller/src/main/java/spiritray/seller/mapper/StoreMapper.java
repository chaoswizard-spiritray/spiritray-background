package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.SellerAccount;
import spiritray.common.pojo.PO.Store;

import java.util.List;

/**
 * ClassName:StoreMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/4/21 15:31
 * @Author:灵@email
 */
@Mapper
@Repository
public interface StoreMapper {
    /*通过电话查询店铺信息*/
    public Store selectStoreByPhone(@Param("phone") long phone);

    /*查询直指定店铺的电话*/
    public Long selectStorePhoneByStoreId(@Param("storeId") String storeId);

    /*通过店铺编号查询店铺信息*/
    public Store selectStoreByStoreId(@Param("storeId") String storeId);

    /*查询店铺关闭信息*/
    public Store selectCloseInfById(@Param("storeId") String storeId);

    /*插入店铺信息*/
    public int insertStore(@Param("store") Store store);

    /*更新店铺表信息*/
    public int updateStore(@Param("store") Store store);

    /*更新店铺状态*/
    public int updateStoreStatusByStoreId(@Param("storeId") String storeId, @Param("status") int status, @Param("causeInf") String causeInf);

    /*查找店铺头像*/
    public String selectStoreHeadById(@Param("storeId") String storeId);

    /*通过店铺id查询店铺营业执照*/
    public List<String> selectLicenseByStoreId(@Param("storeId") String storeId);

    /*插入营业执照*/
    public int insertLicenseByStoreId(@Param("urls") List<String> urls, @Param("storeId") String storeId);

    /*查询指定类型收款账户通过店铺id*/
    public List<SellerAccount> selectSellerAccountByStoreId(@Param("storeId") String storeId, @Param("accaId") int accaId);

    /*插入账户信息*/
    public int insertAccount(@Param("account") SellerAccount account);

    /*修改账户信息*/
    public int updateAccount(@Param("account") SellerAccount account);

    /*删除账户信息*/
    public int deleteAccount(@Param("accountId") int accountId, @Param("storeId") String storeId);

    /*查询指定店铺的指定类型的收款账户*/
    public SellerAccount selectAccountCollectionByStoreIdAndAccaId(@Param("storeId") String storeId, @Param("accaId") int accaId);
}
