package spiritray.common.pojo.PO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:Msg
 * Package:spiritray.common.pojo.PO
 * Description:
 *
 * @Date:2022/11/25 19:14
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Msg {
    private String msgId;
    private Long sender;
    private Long receiver;
    private Integer senderRole;
    private Integer receiverRole;
    private String msg;
    private String msgType;
    private Integer isRead;
    private Timestamp sendDate;
    private Integer isDelete;

}
