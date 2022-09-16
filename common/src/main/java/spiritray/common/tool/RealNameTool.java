package spiritray.common.tool;

import com.alibaba.fastjson.JSONObject;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import spiritray.common.pojo.DTO.RspMsgAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ClassName:RealNameTool
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/4/18 10:45
 * @Author:灵@email
 */
public class RealNameTool {
    public static RspMsgAPI realName(String url, String appCode, Map<String, String> params) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        FormBody.Builder formbuilder = new FormBody.Builder();
        Iterator<String> it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            formbuilder.add(key, params.get(key));
        }
        FormBody body = formbuilder.build();
        Request request = new Request.Builder().url(url).addHeader("Authorization", "APPCODE " + appCode).post(body).build();
        Response response = client.newCall(request).execute();
        String result = response.body().string();
        RspMsgAPI rspMsgAPI = new RspMsgAPI();
        JSONObject jsonObject = JSONObject.parseObject(result);
        jsonObject.forEach((key, value) -> {
            if ("data".equals(key)) {
                //如果是data属性就继续转换变量
                JSONObject jsonObject1 = (JSONObject) value;
                Map map = new HashMap();
                jsonObject1.forEach((key1, value1) -> {
                    map.put(key1, value1);
                });
                rspMsgAPI.setData(map);
            } else {
                if ("msg".equals(key)) {
                    rspMsgAPI.setMsg((String) value);
                }
                if ("success".equals(key)) {
                    rspMsgAPI.setSuccess((Boolean) value);
                }
                if ("code".equals(key)) {
                    rspMsgAPI.setCode((Integer) value);
                }
            }
        });
        //返回的是一个JSON字符串，我们将其转换下
        return rspMsgAPI;
    }
}