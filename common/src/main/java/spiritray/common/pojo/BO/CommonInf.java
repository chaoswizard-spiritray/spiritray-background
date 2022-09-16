package spiritray.common.pojo.BO;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * ClassName:JWTKey
 * Package:spriritray.consumer.pojo.BO
 * Description:
 *
 * @Date:2022/4/14 11:01
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
public class CommonInf {
    private String imgPath;//上传图片路径
    private String sellerSeviceUrl;//seller服务的host
    private String realNameCode;//实名认证密钥
    private String realNameUrl;//实名认证接口
    private String fileHost;//file服务主机名以及服务端口
}
