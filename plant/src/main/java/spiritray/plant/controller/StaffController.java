package spiritray.plant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.plant.service.StaffService;

/**
 * ClassName:StaffController
 * Package:spiritray.plant.controller
 * Description:
 *
 * @Date:2022/6/13 8:35
 * @Author:灵@email
 */
@RestController
@RequestMapping("/plant")
public class StaffController {
    @Autowired
    private StaffService staffService;


    /*获取邮箱验证码*/
    @GetMapping("/logon/code/{staffId}")
    public RpsMsg getCode(@PathVariable long staffId) {
        return staffService.sendCodeByStaffId(staffId);
    }

    /*登录*/
    @GetMapping("/logon/{staffId}/{code}")
    public RpsMsg logon(@PathVariable long staffId, @PathVariable String code) {
        return staffService.logon(staffId, code);
    }
}
