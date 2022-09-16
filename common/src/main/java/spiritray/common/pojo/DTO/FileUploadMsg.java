package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ClassName:FileUploadMsg
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/5/26 14:39
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileUploadMsg {
    private List<String> filePaths;
    private int successNum;
    private int faileNum;
}
