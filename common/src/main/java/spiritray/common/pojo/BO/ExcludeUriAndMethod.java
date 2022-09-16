package spiritray.common.pojo.BO;

import lombok.*;
import lombok.experimental.Accessors;

/**
 * ClassName:ExcludeUriAndMethod
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/4/21 19:06
 * @Author:灵@email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExcludeUriAndMethod {
    private String uris;
    private String methods;

    public String[] getReUris() {
        return uris.replaceAll("[ ]+", "").split(";");
    }

    public String[] getReMethods() {
        return methods.replaceAll("[ ]+", "").split(";");
    }


}
