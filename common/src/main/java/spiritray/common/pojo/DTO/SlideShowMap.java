package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ClassName:SlideShowMap
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/5/27 10:30
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SlideShowMap {
    private List<SlideShow> slideShows;//轮播信息
    private long seconds;//保质期
}
