package spiritray.plant.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.plant.mapper.AccountMapper;
import spiritray.plant.service.AccountService;

/**
 * ClassName:AccountServiceImp
 * Package:spiritray.plant.service.imp
 * Description:
 *
 * @Date:2022/5/24 18:57
 * @Author:灵@email
 */
@Service
public class AccountServiceImp implements AccountService {
    @Autowired
    private AccountMapper accountMapper;

    @Override
    public RpsMsg queryAccountCategory() {
        return new RpsMsg().setData(accountMapper.selectAccountCategory()).setMsg("查询成功").setStausCode(200);
    }

    @Override
    public RpsMsg queryAccountByCate(int cate, int type) {
        return new RpsMsg().setData(accountMapper.selectAccount(cate, type)).setStausCode(200);
    }
}
