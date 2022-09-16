package spiritray.seller.service;

import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.SellerAccount;
import spiritray.common.pojo.PO.Store;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ClassName:StoreService
 * Package:spiritray.seller.service
 * Description:
 *
 * @Date:2022/4/21 15:43
 * @Author:灵@email
 */
public interface StoreService {

    /*修改店铺信息*/
    public RpsMsg modifyStore(Store store, MultipartFile file);

    /*修改店铺状态*/
    public RpsMsg modifyStoreStatusByStoreId(String storeId, int status, String causeInf);

    /*查找店铺信息通过电话*/
    public RpsMsg findStoreByPhone(long phone, HttpSession session);

    /*查询店铺信息通过店铺id*/
    public RpsMsg findStoreByStoreId(String storeId);

    /*查询当前店铺关闭的信息*/
    public RpsMsg queryCloseInf(String storeId);

    /*查询当前店铺已上传的执照*/
    public RpsMsg queryLicenseByStoreId(String storeId);

    /*添加执照到当前店铺*/
    public RpsMsg addLicense(List<MultipartFile> files, String storeId);

    /*查询店铺的所有收款账户信息*/
    public RpsMsg queryAccountsByStoreId(String storeId, int accaId);

    /*添加收款账户*/
    public RpsMsg addAccount(SellerAccount account);

    /*修改账户信息*/
    public RpsMsg modifyAccount(SellerAccount account);

    /*删除账户信息*/
    public RpsMsg removeAccount(int accountId, String storeId);

    /*通过店铺号和账户类别查询收款账户信息*/
    public RpsMsg queryCollectionAccountByStoreIdAndAccaId(String storeId, int accaId);
}
