package spiritray.common.pojo.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import spiritray.common.pojo.PO.Consumer;

/**
 * ClassName:RegisterDTO
 * Package:spriritray.consumer.pojo.DTO
 * Description:
 *
 * @Date:2022/4/14 13:35
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
public class RegisterDTO {
    private Consumer consumer;
    private String code;
}
