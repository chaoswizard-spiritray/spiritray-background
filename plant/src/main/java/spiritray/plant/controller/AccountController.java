package spiritray.plant.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.AccountCategory;
import spiritray.common.pojo.PO.PlantAccount;
import spiritray.plant.mapper.AccountMapper;
import spiritray.plant.service.AccountService;

/**
 * ClassName:AccountController
 * Package:spiritray.plant.controller
 * Description:
 *
 * @Date:2022/5/24 17:54
 * @Author:灵@email
 */
@RestController
@RequestMapping("/plant")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountMapper accountMapper;

    /*获取平台账户种类*/
    @GetMapping("/account/category")
    public RpsMsg getAccountCategory() {
        return accountService.queryAccountCategory();
    }

    /*获取指定类型的可用账户*/
    @GetMapping("/account/useable/{cate}")
    public RpsMsg getAccountUseableByCate(@PathVariable int cate) {
        return accountService.queryAccountByCate(cate, 2);
    }

    /*获取平台所有可用账户*/
    @GetMapping("/account/all/{cate}")
    public RpsMsg getPlantAccountAll(@PathVariable int cate) {
        return accountService.queryAccountByCate(cate, 0);
    }

    /*修改支付方式状态*/
    @PutMapping("/account/category")
    public RpsMsg putAccountCategory(String cate) {
        AccountCategory accountCategory = JSON.parseObject(cate, AccountCategory.class);
        if (accountMapper.updateAccountCategoryById(accountCategory) > 0) {
            return new RpsMsg().setMsg("切换成功").setStausCode(200);
        } else {
            return new RpsMsg().setMsg("切换失败").setStausCode(300);
        }
    }

    /*删除指定账户*/
    @PutMapping("/account/delete")
    public RpsMsg deleteAccount(int paId) {
        if (accountMapper.deleteAccount(paId) > 0) {
            return new RpsMsg().setMsg("删除成功").setStausCode(200);
        } else {
            return new RpsMsg().setMsg("删除失败").setStausCode(300);
        }
    }

    /*添加账户*/
    @PostMapping("/account/add")
    public RpsMsg addAccount(String account) {
        PlantAccount plantAccount = JSON.parseObject(account, PlantAccount.class);
        if (accountMapper.insertAccount(plantAccount) > 0) {
            return new RpsMsg().setMsg("添加成功").setStausCode(200);
        } else {
            return new RpsMsg().setMsg("添加失败").setStausCode(300);
        }
    }

    /*修改账户*/
    @PutMapping("/account/modify")
    public RpsMsg modifyAccount(String account) {
        PlantAccount plantAccount = JSON.parseObject(account, PlantAccount.class);
        if (accountMapper.updateAccount(plantAccount) > 0) {
            return new RpsMsg().setMsg("修改成功").setStausCode(200);
        } else {
            return new RpsMsg().setMsg("修改失败").setStausCode(300);
        }
    }

}
