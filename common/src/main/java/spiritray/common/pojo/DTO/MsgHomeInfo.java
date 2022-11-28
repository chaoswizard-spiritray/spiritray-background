package spiritray.common.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.sql.Timestamp;

/**
 * ClassName:MsgHomeInfo
 * Package:spiritray.common.pojo.DTO
 * Description:
 *
 * @Date:2022/11/25 12:35
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MsgHomeInfo {
    private Long sender;
    private Integer senderRole;
    private String sendHead;//发送者头像
    private String sendName;//发送者名称
    private String lastestMsg;//最近一条交互消息
    private String lastestMsgType;//这条消息的类型
    private Integer lastestMsgIsRead;//这条消息的阅读状态
    private Timestamp lastestMsgDate;//这条消息的交互时间
}
