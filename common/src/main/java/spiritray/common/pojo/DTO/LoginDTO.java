package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import spiritray.common.pojo.PO.Consumer;

/**
 * ClassName:LoginDTO
 * Package:spiritray.consumer.pojo.DTO
 * Description:
 *
 * @Date:2022/4/14 17:29
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private Consumer consumer;
    private String jwt;
}
