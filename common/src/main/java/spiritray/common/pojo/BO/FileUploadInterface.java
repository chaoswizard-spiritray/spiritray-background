package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * ClassName:FileUploadInterface
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/5/29 15:57
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FileUploadInterface {
    private String FILE_UPLOAD_SIMPLE;
    private String FILE_UPLOAD_MUL;
}
