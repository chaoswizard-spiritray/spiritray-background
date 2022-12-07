package spiritray.seller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;

import javax.servlet.http.HttpSession;

/**
 * ClassName:StoreCloseController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/12/1 21:41
 * @Author:灵@email
 */
@RestController
@RequestMapping("/store/close")
public class StoreCloseController {


    /*商家申请关闭店铺*/
    @PostMapping("/seller/apply/close")
    public RpsMsg postApplyCloseInfo(HttpSession session) {
    return null;
    }

    /*平台强制关闭店铺*/
    @PostMapping("/plant/force/close")
    public RpsMsg postPlantForceClose() {

        return null;
    }

    /*平台解封店铺*/
    @PutMapping("/plant/open")
    public RpsMsg putOpenStore(){
        return null;
    }
}
