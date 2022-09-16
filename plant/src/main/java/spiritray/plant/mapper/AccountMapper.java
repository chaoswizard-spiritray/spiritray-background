package spiritray.plant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.AccountCategory;
import spiritray.common.pojo.PO.PlantAccount;

import java.util.List;

/**
 * ClassName:AccountMapper
 * Package:spiritray.plant.mapper
 * Description:
 *
 * @Date:2022/5/24 19:00
 * @Author:灵@email
 */
@Mapper
@Repository
public interface AccountMapper {
    /*查询平台账户类型*/
    public List<AccountCategory> selectAccountCategory();

    /*修改指定支付类型状态*/
    public int updateAccountCategoryById(@Param("cate") AccountCategory accountCategory);

    /*查询平台账户*/
    public List<PlantAccount> selectAccount(@Param("cate") int cate, @Param("type") int type);

    /*删除指定账户*/
    public int deleteAccount(@Param("paId") int paId);

    /*插入账户信息*/
    public int insertAccount(@Param("account") PlantAccount account);

    /*修改账户*/
    public int updateAccount(@Param("account") PlantAccount account);
}
