package spiritray.seller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInf;
import spiritray.common.pojo.BO.ExcludeUriAndMethod;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Seller;
import spiritray.seller.service.SellerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Stream;

/**
 * ClassName:SellerController
 * Package:spiritray.consumer.controller
 * Description:
 *
 * @Date:2022/4/17 16:02
 * @Author:灵@email
 */
@RestController
@RequestMapping("/seller")
public class SellerController {
    @Autowired
    private SellerService sellerService;


    /*检测是否入驻*/
    @GetMapping("/enter/{phone}")
    public RpsMsg checkEnter(@PathVariable long phone) {
        return sellerService.checkEnter(phone);
    }

    /*入驻*/
    @PostMapping("/enter")
    public RpsMsg addSeller(Seller seller, MultipartFile file, HttpSession session) {
        seller.setSellerPhone((Long) session.getAttribute("phone"));
        return sellerService.enter(seller, file);
    }
}
