package spiritray.seller.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.StoreLicenseSimple;
import spiritray.common.pojo.PO.Seller;
import spiritray.common.pojo.PO.SellerAccount;
import spiritray.common.pojo.PO.Store;
import spiritray.seller.service.StoreService;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ClassName:StoreController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/4/21 16:52
 * @Author:灵@email
 */
@RestController
@RequestMapping("/store")
public class StoreController {
    @Autowired
    private StoreService storeService;

    /*通过电话查询当前登录者的店铺信息*/
    @GetMapping("/storeInf/phone")
    public RpsMsg getStoreInfByPhone(HttpSession session) {
        return storeService.findStoreByPhone((Long) session.getAttribute("phone"), session);
    }

    /*通过店铺编号查询店铺信息*/
    @GetMapping("/storeInf/{storeId}")
    public RpsMsg getStoreInfByStoreId(@PathVariable String storeId) {
        return storeService.findStoreByStoreId(storeId);
    }

    /*查询当前店铺被关闭的信息*/
    @GetMapping("/closeInf")
    public RpsMsg getCloseInf(HttpSession session) {
        return storeService.queryCloseInf((String) session.getAttribute("storeId"));
    }

    /*修改当前店铺信息*/
    @PutMapping("/storeInf")
    public RpsMsg putStoreInf(Store store, MultipartFile file) {
        return storeService.modifyStore(store, file);
    }

    /*关闭当前店铺*/
    @PutMapping("/storeInf/status/down")
    public RpsMsg shutdown(HttpSession session, String causeInf) {
        return storeService.modifyStoreStatusByStoreId((String) session.getAttribute("storeId"), 0, causeInf);
    }

    /*店铺解封,这个接口不走JWT等拦截器，只走平台管理员拦截器*/
    @PutMapping("/storeInf/status/open")
    public RpsMsg shutdown(String storeId) {
        return storeService.modifyStoreStatusByStoreId(storeId, 1, null);
    }

    /*上传店铺执照*/
    @PostMapping("/license")
    public RpsMsg uploadLicense(List<MultipartFile> files, HttpSession session) {
        return storeService.addLicense(files, (String) session.getAttribute("storeId"));
    }

    /*查询当前店铺的相关执照*/
    @GetMapping("/storeInf/license/{storeId}")
    public RpsMsg getLicenseByStoreId(@PathVariable String storeId) {
        return storeService.queryLicenseByStoreId(storeId);
    }

    /*获取当前店铺所有账户信息*/
    @GetMapping("/account/{accaId}")
    public RpsMsg getAccounts(HttpSession session, @PathVariable int accaId) {
        return storeService.queryAccountsByStoreId((String) session.getAttribute("storeId"), accaId);
    }

    /*添加收款账户*/
    @PostMapping("/account")
    public RpsMsg postAccount(String account, HttpSession session) {
        SellerAccount sellerAccount = JSON.parseObject(account, SellerAccount.class);
        sellerAccount.setStoreId((String) session.getAttribute("storeId"));
        return storeService.addAccount(sellerAccount);
    }

    /*修改账户信息*/
    @PutMapping("/account")
    public RpsMsg putAccount(String account, HttpSession session) {
        SellerAccount sellerAccount = JSON.parseObject(account, SellerAccount.class);
        sellerAccount.setStoreId((String) session.getAttribute("storeId"));
        return storeService.modifyAccount(sellerAccount);
    }


    /*删除账户*/
    @DeleteMapping("/account")
    public RpsMsg deleteAccount(int accountId, HttpSession session) {
        return storeService.removeAccount(accountId, (String) session.getAttribute("storeId"));
    }

    /*获取指定账户类型的正在使用的收款账户*/
    @GetMapping("/account/{storeId}/{accaId}")
    public RpsMsg getAccountByAccaId(@PathVariable String storeId, @PathVariable int accaId) {
        return storeService.queryCollectionAccountByStoreIdAndAccaId(storeId, accaId);
    }

    /*获取指定店铺携带营业执照信息*/
    @GetMapping("/storeLicenseSimple/{storeId}")
    public RpsMsg getStoreLicenseSimple(@PathVariable String storeId) {
        List<String> list = (List<String>) storeService.queryLicenseByStoreId(storeId).getData();
        Store store = (Store) storeService.findStoreByStoreId(storeId).getData();
        //封装信息
        return new RpsMsg().setStausCode(200).setMsg("查询成功")
                .setData(new StoreLicenseSimple().setLicense(list).setStoreHead(store.getStoreHead())
                        .setStoreName(store.getStoreName()));
    }
}
