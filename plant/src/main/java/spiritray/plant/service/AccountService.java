package spiritray.plant.service;

import spiritray.common.pojo.DTO.RpsMsg;

/**
 * ClassName:AccountService
 * Package:spiritray.plant.service
 * Description:
 *
 * @Date:2022/5/24 18:50
 * @Author:灵@email
 */
public interface AccountService {
    /*查询平台账户种类*/
    public RpsMsg queryAccountCategory();

    /*查询指定种类账户,type:0表示全部，1表示不能使用，2表示能够使用的*/
    public RpsMsg queryAccountByCate(int cate,int type);
}
