package spiritray.common.tool;

import org.springframework.mail.SimpleMailMessage;

/**
 * ClassName:EmailSendTool
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/5/28 12:42
 * @Author:灵@email
 */
public class EmailSendTool {
    /*获取简单邮件信息对象*/
    public static SimpleMailMessage getSimpleMailMessage(String sendFrom, String sendTo, String subject, String text) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(sendFrom);
        simpleMailMessage.setTo(sendTo);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);
        return simpleMailMessage;
    }
}
