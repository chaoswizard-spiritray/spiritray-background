package spiritray.plant.service;

import spiritray.common.pojo.DTO.RpsMsg;

/**
 * ClassName:StaffService
 * Package:spiritray.plant.service
 * Description:
 *
 * @Date:2022/6/13 8:39
 * @Author:灵@email
 */
public interface StaffService {

    /*像指定工号所绑定的邮箱发送验证码*/
    public RpsMsg sendCodeByStaffId(long staffId);

    /*员工登录*/
    public RpsMsg logon(long staffId, String code);
}
