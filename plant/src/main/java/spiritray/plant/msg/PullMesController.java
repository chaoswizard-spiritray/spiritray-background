package spiritray.plant.msg;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.MsgHomeInfo;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Msg;
import spiritray.plant.mapper.MsgMapper;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ClassName:PullMesController
 * Package:spiritray.plant.msg
 * Description:
 * https://www.coder.work/article/1819162
 * 商家、买家可以进行消息交互，并且可以接收系统消息，而平台管理员只能接收系统消息
 *
 * @Date:2022/11/25 12:23
 * @Author:灵@email
 */
@RestController
@RequestMapping("/msg")
public class PullMesController {
    @Autowired
    private MsgMapper msgMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private HttpHeaders httpHeaders;

    @Autowired
    @Qualifier("consumerPWMap")
    private ConcurrentHashMap<Long, Session> consumerPWMap;

    @Autowired
    @Qualifier("sellerPWMap")
    private ConcurrentHashMap<Long, Session> sellerPWMap;

    @Autowired
    @Qualifier("plantPWMap")
    private ConcurrentHashMap<Long, Session> plantPWMap;

    @Autowired
    @Qualifier("stayMsgHomeConsumer")
    private ConcurrentHashMap<Long, Session> stayMsgHomeConsumer;

    @Autowired
    @Qualifier("stayMsgHomeSeller")
    private ConcurrentHashMap<Long, Session> stayMsgHomeSeller;

    @Autowired
    @Qualifier("stayMsgHomePlant")
    private ConcurrentHashMap<Long, Session> stayMsgHomePlant;

    @Autowired
    @Qualifier("stayMsgDetailConsumer")
    private ConcurrentHashMap<Long, Session> stayMsgDetailConsumer;

    @Autowired
    @Qualifier("stayMsgDetailSeller")
    private ConcurrentHashMap<Long, Session> stayMsgDetailSeller;

    @Autowired
    @Qualifier("stayMsgDetailPlant")
    private ConcurrentHashMap<Long, Session> stayMsgDetailPlant;


    private final String CONSUMER_URL = "http://localhost:8080";

    private final String SELLER_URL = "http://localhost:8081";

    private Comparator comparator = new Comparator<MsgHomeInfo>() {
        @Override
        public int compare(MsgHomeInfo o1, MsgHomeInfo o2) {
            if (o1.getSenderRole() == 0) {
                return -1;
            } else if (o2.getSenderRole() == 0) {
                return 1;
            } else {
                //如果两者不是系统
                if (o1.getLastestMsgIsRead() == 0) {
                    if (o2.getLastestMsgIsRead() == 0) {
                        //如果两者都是未读消息，就比较时间
                        if (o1.getLastestMsgDate().getTime() > o2.getLastestMsgDate().getTime()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return -1;
                    }
                } else {
                    if (o2.getLastestMsgIsRead() == 1) {
                        //如果两者都是未读消息，就比较时间
                        if (o1.getLastestMsgDate().getTime() > o2.getLastestMsgDate().getTime()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return -1;
                    }
                }
            }
        }
    };

    /*用户进入消息界面时获取大概信息MsgHomeInfo
     * 拉取消息的角色：0代表平台，1代表买家，2代表商家
     * */
    @GetMapping("/home/{receiver}/{role}")
    public RpsMsg pullMsgPageInfo(@PathVariable Long receiver, @PathVariable int role) {
//        try {
        List<MsgHomeInfo> msgHomeInfos = msgMapper.selectMsgHomeInfoByReceiver(receiver, role, role == 0 ? 0 : (role == 1 ? 2 : 1));
        if (msgHomeInfos == null || msgHomeInfos.size() == 0) {
            return new RpsMsg().setStausCode(200);
        } else {
            //如果是平台管理员在拉取消息，我们直接返回，并且它只有系统消息
            if (role == 0) {
                return new RpsMsg().setStausCode(200).setData(msgHomeInfos);
            }
            //先过滤出非系统消息的sender部分
            List<Long> senders = msgHomeInfos.stream().filter(s -> s.getSender() != 0).map(MsgHomeInfo::getSender).collect(Collectors.toList());
            //获取头像和名称
            if (role == 1) {
                //如果拉取信息的是买家，那么就查询商家的店铺名和头像
                MultiValueMap multiValueMap = new LinkedMultiValueMap();
                ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(SELLER_URL + "/store/headAndName/many?phones=" + JSON.toJSONString(senders), HttpMethod.GET, new HttpEntity<>(multiValueMap, httpHeaders), RpsMsg.class);
                Map<Long, SSMap> mapMap = JSONObject.parseObject(JSON.toJSONString(responseEntity.getBody().getData()), HashMap.class);
                for (MsgHomeInfo msgHomeInfo : msgHomeInfos) {
                   final long s=msgHomeInfo.getSender();
                    System.out.println(mapMap.get(s).getAttributeValue());
                   msgHomeInfo.setSendName(mapMap.get(s).getAttributeValue());
                   msgHomeInfo.setSendHead(mapMap.get(msgHomeInfo.getSender().longValue()).getAttributeName());
                }
                //排序将系统放在最前面，其他按照未读、时间优先级进行排序
                msgHomeInfos.sort(comparator);
                return new RpsMsg().setData(msgHomeInfos).setMsg("查询成功").setStausCode(200);
            }
            if (role == 2) {
                //如果拉取信息的是商家，那么就查询买家的昵称和头像
                MultiValueMap multiValueMap = new LinkedMultiValueMap();
                ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(CONSUMER_URL + "/consumer/headAndName/many?consumerPhones=" + JSON.toJSONString(senders), HttpMethod.GET, new HttpEntity<>(multiValueMap, httpHeaders), RpsMsg.class);
                Map<Long, SSMap> mapMap = (Map<Long, SSMap>) responseEntity.getBody().getData();
                for (MsgHomeInfo msgHomeInfo : msgHomeInfos) {
                    mapMap.entrySet().stream().peek(s -> {
                        if (s.getKey().longValue() == msgHomeInfo.getSender().longValue()) {
                            msgHomeInfo.setSendName(mapMap.get(msgHomeInfo.getSender().longValue()).getAttributeValue());
                            msgHomeInfo.setSendHead(mapMap.get(msgHomeInfo.getSender().longValue()).getAttributeName());
                        }
                    }).collect(Collectors.toList());
                }
                msgHomeInfos.sort(comparator);
                return new RpsMsg().setData(msgHomeInfos).setMsg("查询成功").setStausCode(200);
            }
            return new RpsMsg().setStausCode(300).setMsg("违规操作");
        }
//        } catch (Exception e) {
//            return new RpsMsg().setStausCode(300).setMsg("系统异常");
//        }
    }

    /*当前会话用户获取指定发送者的消息*/
    @GetMapping("/{sender}/{pageNo}/{pageNum}")
    public RpsMsg getSenderMsg(HttpSession session, @PathVariable Long sender, @PathVariable Integer pageNo,
                               @PathVariable int pageNum) {
        List<Msg> temp = msgMapper.selectMsgBySenderAndPage((Long) session.getAttribute("phone"), sender, pageNo, pageNum);
        temp.sort(new Comparator<Msg>() {
            @Override
            public int compare(Msg o1, Msg o2) {
                if (o1.getSendDate().getTime() > o2.getSendDate().getTime()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return new RpsMsg().setData(temp).setStausCode(200);
    }

    /*删除指定消息*/
    @DeleteMapping("/simple/{msgId}")
    public RpsMsg deleteMsg(@PathVariable String msgId, HttpSession session) {
        msgMapper.updateMsgIsDelete((Long) session.getAttribute("phone"), msgId);
        return new RpsMsg().setStausCode(200).setMsg("消息已移除");
    }

    /*删除指定发送者的所有消息*/
    @DeleteMapping("/all/{senderId}")
    public RpsMsg deleteSenderAllMsg(@PathVariable Long senderId, HttpSession session) {
        msgMapper.updateAllDeleteBySenderIdAndReceiver((Long) session.getAttribute("phone"), senderId);
        return new RpsMsg().setStausCode(200).setMsg("消息已移除");
    }

    /*修改指定消息的阅读状态*/
    @PutMapping("/readed/{msgId}")
    public RpsMsg readedMsg(@PathVariable String msgId, HttpSession session) {
        msgMapper.updateMsgIsDelete((Long) session.getAttribute("phone"), msgId);
        return new RpsMsg().setStausCode(200).setMsg("消息已读");
    }

    /*发送消息*/
    @PostMapping("/send")
    public RpsMsg addMsg(String msg, MultipartFile file) {
        //如果有发送图片，需要将图片上传后再将文件插入
        Msg msg1 = JSON.parseObject(msg, Msg.class);
        if (file != null) {
            String url = "/static/msg";
            String fileName = String.valueOf(UUID.randomUUID()) + RandomUtil.randomString(2);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            CommonInputStreamResource commonInputStreamResource = null;
            try {
                commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.add("path", url);
            map.add("file", commonInputStreamResource);
            map.add("fileName", fileName);
            HttpEntity httpEntity = new HttpEntity(map, httpHeaders);
            String saveUrl = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class).getBody();
            if (saveUrl == null) {
                return new RpsMsg().setMsg("发送失败").setStausCode(300);
            }
            //如果上传成功，修改发送的信息类型以及数据
            msg1.setMsgType("img");
            msg1.setMsg(saveUrl);
        }
        //设置主键
        msg1.setMsgId(String.valueOf(UUID.randomUUID()));
        //保存信息
        int i = msgMapper.insertMsgSimple(msg1);
        if (i == 1) {
            return new RpsMsg().setMsg("发送成功").setStausCode(200).setData(msg1);
        }
        return new RpsMsg().setStausCode(300).setMsg("发送失败");
    }

    /*消息推送逻辑*/
    private void pushMsg(Msg msg) {
        //先确定接收者角色
        Integer receiverRole = msg.getReceiverRole();
        switch (receiverRole) {
            case 0: {
                //如果是平台接收
                Long receiver = msg.getReceiver();
                //先判断是否在应用
                if (!isUseApp(receiver, plantPWMap)) {
                    //如果没有在线，就直接不用推送，结束逻辑
                    return;
                }
                //如果在线，就判断更细节的位置
                boolean success = sendMsg(receiver, stayMsgDetailPlant, JSON.toJSONString(msg));
                //如果并不在就看是否在消息标签页
                if (!success) {
                    success = sendMsg(receiver, stayMsgHomePlant, JSON.toJSONString(msg));
                    if (!success) {
                        //如果不在标签页，就最后执行一次sendMsg
                        sendMsg(receiver, plantPWMap, JSON.toJSONString(msg));
                    }
                }
            }
            break;
            case 1: {
                //如果是平台接收
                Long receiver = msg.getReceiver();
                //先判断是否在应用
                if (!isUseApp(receiver, consumerPWMap)) {
                    //如果没有在线，就直接不用推送，结束逻辑
                    return;
                }
                //如果在线，就判断更细节的位置
                boolean success = sendMsg(receiver, stayMsgDetailConsumer, JSON.toJSONString(msg));
                //如果并不在就看是否在消息标签页
                if (!success) {
                    success = sendMsg(receiver, stayMsgHomeConsumer, JSON.toJSONString(msg));
                    if (!success) {
                        //如果不在标签页，就最后执行一次sendMsg
                        sendMsg(receiver, consumerPWMap, JSON.toJSONString(msg));
                    }
                }
            }
            break;
            case 2: {
                //如果是平台接收
                Long receiver = msg.getReceiver();
                //先判断是否在应用
                if (!isUseApp(receiver, sellerPWMap)) {
                    //如果没有在线，就直接不用推送，结束逻辑
                    return;
                }
                //如果在线，就判断更细节的位置
                boolean success = sendMsg(receiver, stayMsgDetailSeller, JSON.toJSONString(msg));
                //如果并不在就看是否在消息标签页
                if (!success) {
                    success = sendMsg(receiver, stayMsgHomeSeller, JSON.toJSONString(msg));
                    if (!success) {
                        //如果不在标签页，就最后执行一次sendMsg
                        sendMsg(receiver, sellerPWMap, JSON.toJSONString(msg));
                    }
                }
            }
            break;

        }
    }

    //判断接收者是否在某一个连接
    private boolean isUseApp(Long receiverId, ConcurrentHashMap<Long, Session> concurrentHashMap) {
        if (concurrentHashMap.get(receiverId) == null) {
            return false;
        } else {
            return true;
        }
    }


    //遍历推送
    private boolean sendMsg(Long receiverId, ConcurrentHashMap<Long, Session> concurrentHashMap, String msg) {
        Session session = concurrentHashMap.get(receiverId);
        if (session == null) {
            //如果不存在
            return false;
        } else {
            try {
                session.getBasicRemote().sendText(msg);
                return true;
            } catch (IOException e) {
                return true;
            }
        }
    }


}
