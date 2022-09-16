package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * ClassName:InSellDetail
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/4/27 11:14
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InSellDetail {
    private String commodityId;
    private String commodityName;
    private String categoryName;
    private String masterMap;
    private String commodityDescribe;
    private String address;
    private Date publishDate;
}
