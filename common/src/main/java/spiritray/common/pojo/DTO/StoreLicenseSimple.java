package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * ClassName:StoreLicenseSimple
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/6/13 21:41
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class StoreLicenseSimple {
    private String storeName;
    private String storeHead;
    private List<String> license;
}
