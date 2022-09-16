package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:SlideShow
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/5/26 21:53
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SlideShow {
    private String storeId;
    private String mapUrl;
}
