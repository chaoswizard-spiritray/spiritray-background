package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:HotWord
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/12/10 18:32
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HotWord {
    private String hotWord;
    private Long score;
    private Timestamp date;
}
