package spiritray.seller.service;

import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Seller;

import javax.servlet.http.HttpServletRequest;

/**
 * ClassName:SellerService
 * Package:spiritray.consumer.service
 * Description:
 *
 * @Date:2022/4/17 16:05
 * @Author:灵@email
 */
public interface SellerService {
    /*检测是否入驻*/
    public RpsMsg checkEnter(long phone);

    /*商家入驻*/
    public RpsMsg enter(Seller seller, MultipartFile file);


}
