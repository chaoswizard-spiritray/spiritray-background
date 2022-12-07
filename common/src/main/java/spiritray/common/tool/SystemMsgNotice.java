package spiritray.common.tool;

import com.alibaba.fastjson.JSON;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Msg;

/**
 * ClassName:SystemMsgNotice
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/11/29 11:21
 * @Author:灵@email
 */
public class SystemMsgNotice {
    private static final String MSG_Send_URL = "http://localhost:8083/msg/send";

    public static RpsMsg notieMsg(RestTemplate restTemplate, Msg msg) {
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("msg", JSON.toJSONString(msg));
        return restTemplate.exchange(MSG_Send_URL, HttpMethod.POST, new HttpEntity<>(multiValueMap, new HttpHeaders()), RpsMsg.class).getBody();
    }
}
