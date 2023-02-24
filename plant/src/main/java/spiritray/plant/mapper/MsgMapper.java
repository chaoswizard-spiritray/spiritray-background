package spiritray.plant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.MsgHomeInfo;
import spiritray.common.pojo.PO.Msg;

import java.util.List;

/**
 * ClassName:MsgMapper
 * Package:spiritray.plant.mapper
 * Description:
 *
 * @Date:2022/11/25 12:32
 * @Author:灵@email
 */
@Mapper
@Repository
public interface MsgMapper {
    /*查询指定接收者及角色的首页展示系统消息*/
    public  MsgHomeInfo selectSysytemMsgHomeInfoByReceiver(@Param("receiver") Long receiver, @Param("role") Integer role);
    /*查询消息首页展示的数据MsgHomeInfo*/
    public List<MsgHomeInfo> selectMsgHomeInfoByReceiver(@Param("receiver") Long receiver, @Param("role") Integer role);

    /*删除指定发送者的所有消息*/
    public int updateAllDeleteBySenderIdAndReceiver(@Param("receiver") Long receiver, @Param("sender") Long sender);

    /*查询指定发送者的消息*/
    public List<Msg> selectMsgBySenderAndPage(@Param("receiver") Long receive, @Param("sender") Long sender, @Param("pageNo") Integer pageNo, @Param("pageNum") Integer pageNum);

    /*删除指定消息*/
    public int updateMsgIsDelete(@Param("receiver") Long receiver, @Param("msgId") String msgId);

    /*插入指定消息*/
    public int insertMsgSimple(@Param("msg") Msg msg);

    /*修改指定消息为已读*/
    public int updateMsgReaded( @Param("msgId") String msgId);
}
