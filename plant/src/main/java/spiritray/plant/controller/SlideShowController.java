package spiritray.plant.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.factory.SlideFactory;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SNMap;
import spiritray.common.pojo.DTO.SlideShow;
import spiritray.common.pojo.DTO.SlideShowMap;
import spiritray.common.pojo.PO.Slide;
import spiritray.plant.mapper.SlideMapper;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName:SlideShowController
 * Package:spiritray.plant.controller
 * Description:
 * 轮播信息很少并且一段时间后会变化，所以我们存放到redis中
 *
 * @Date:2022/5/26 21:51
 * @Author:灵@email
 */
@RestController
@RequestMapping("/plant")
public class SlideShowController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private HttpHeaders headers;

    @Autowired
    @Qualifier("slideFactory")
    private SlideFactory slideFactory;

    @Autowired
    @Qualifier("threadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private SlideMapper slideMapper;

    private String SLIDEIMGPATH = "/static/slide";


    /*配置轮播图工厂并，启动*/
    @PostMapping("/slide/factory/config")
    public RpsMsg configSlideFactory(int workNumber, int idleNum, int idleTimeUnit, int productSlideNum) {
        //配置工厂
        try {
            slideFactory.setWorkNumber(workNumber);//工作次数
            slideFactory.setIdleNum(idleNum);//每次的工作时长
            slideFactory.setGetStores(new ConcurrentHashMap<>());//清除上一次的获奖者
            long workMill = idleNum;//工作毫秒数
            switch (idleTimeUnit) {
                case 0: {
                    //秒
                    slideFactory.setIdleUnit(TimeUnit.SECONDS);
                    workMill = (workMill << 3) * 125L;
                }
                break;
                case 1: {
                    //分
                    slideFactory.setIdleUnit(TimeUnit.MINUTES);
                    workMill = (workMill << 5) * 125L * 15L;
                }
                break;
                case 2: {
                    //时
                    slideFactory.setIdleUnit(TimeUnit.HOURS);
                    workMill = (workMill << 7) * 125L * 15 * 15;
                }
                break;
                case 3: {
                    //天
                    slideFactory.setIdleUnit(TimeUnit.DAYS);
                    workMill = (workMill << 10) * 125L * 15 * 15 * 3;
                }
                break;
            }
            slideFactory.setProductNum(productSlideNum);//生产数目
            //创建队列立即执行
            final long temp = workMill;
            threadPoolExecutor.execute(new Thread(new Runnable() {
                @Override
                public void run() {
                    //启动工厂
                    slideFactory.setState(0);
                    for (int i = 0; i < slideFactory.getWorkNumber(); i++) {
                        //设置下次发放日期
                        slideFactory.setNextPublishDate(new Date(new Date().getTime() + temp));
                        //进入生产
                        ThreadUtil.sleep(idleNum, slideFactory.getIdleUnit());
                        slideFactory.getProductSurplus().set(slideFactory.getProductNum());//设置剩余数量
                        slideFactory.setPublishNo(String.valueOf(UUID.randomUUID()));//产生发放编号
                        slideFactory.getGetStores().put(slideFactory.getPublishNo(), new ConcurrentHashMap<>());//初始化获取集合
                        //生产完毕,进入发放
                        slideFactory.getLock().lock();//上锁
                        slideFactory.setState(1);//设置发放状态
                        //等待被抢购
                        try {
                            slideFactory.getFactoryCondition().await();//线程进入等待，当剩余数量抢购完后会被唤醒
                            //如果是最后一次，就关闭工厂
                            if (i == slideFactory.getWorkNumber() - 1) {
                                slideFactory.setState(-1);//唤醒后设置状态
                            } else {
                                slideFactory.setState(0);
                            }
                            slideFactory.getLock().unlock();//释放锁进入下一次生产循环
                            //将中奖记录写入系统
                            List<Slide> slides = new ArrayList<>();
                            slideFactory.getGetStores().get(slideFactory.getPublishNo()).entrySet().stream().peek(
                                    s -> {
                                        slides.add(new Slide().setSlideNo(slideFactory.getPublishNo()).setStoreId(s.getKey()).setGetDate(new Timestamp(new Date().getTime())));
                                    }
                            ).count();
                            slideMapper.insertSlides(slides);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            slideFactory.getLock().unlock();
                        }
                    }
                    //休眠最后一段时间，让轮播图结束
                    ThreadUtil.sleep(idleNum, slideFactory.getIdleUnit());
                    //清除轮播图
                    clearSlide();
                    slideFactory.setGetStores(new ConcurrentHashMap<>());//清除轮播图
                    slideFactory.setPublishNo(null);
                }
            }));
            return new RpsMsg().setStausCode(200).setMsg("启动成功");
        } catch (Exception e) {
            //返回信息
            return new RpsMsg().setStausCode(300).setMsg("启动失败");
        }
    }

    /*活动结束清除上传的轮播图*/
    private boolean clearSlide() {
        //得到所有路径
        try {
            slideFactory.getGetStores().entrySet().stream().peek(
                    s -> {
                        s.getValue().entrySet().stream().peek(
                                m -> {
                                    if (m.getValue() != "") {
                                        MultiValueMap multiValueMap = new LinkedMultiValueMap();
                                        multiValueMap.set("path", m.getValue());
                                        restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, new HttpEntity<>(multiValueMap, new HttpHeaders()), String.class).getBody();
                                    }
                                }
                        ).count();
                    }
            ).count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /*查询轮播图工厂信息*/
    @GetMapping("/slide/factory/info")
    public RpsMsg slideFactory() {
        Map<String, Object> map = new HashMap<>();
        map.put("nextPublishDate", slideFactory.getNextPublishDate());
        map.put("state", slideFactory.getState());
        List<String> storeIds = new ArrayList<>();
        if (slideFactory.getPublishNo() != null) {
            map.put("publishNo", slideFactory.getPublishNo());
            slideFactory.getGetStores().get(slideFactory.getPublishNo()).entrySet().stream()
                    .peek(s -> {
                        storeIds.add(s.getKey());
                    }).count();
            if (storeIds.size() > 0) {
                map.put("storeIds", JSON.toJSONString(storeIds));
            }
        }
        return new RpsMsg().setStausCode(200).setData(map);
    }

    /*抢取轮播名额*/
    @PostMapping("/slide/store/get")
    public RpsMsg getSlide(HttpSession session) {
        //获取storeId
        String storeId = (String) session.getAttribute("storeId");
        //判断是否已经获取到了
        if (slideFactory.getGetStores().get(slideFactory.getPublishNo()).get(storeId) != null) {
            return new RpsMsg().setStausCode(300).setMsg("你已经抢到了，快去上传图片吧");
        }
        //如果没有抢到，先判断工厂状态
        if (slideFactory.getState() == 1) {
            //获取剩余数量
            if (slideFactory.getProductSurplus().get() > 0) {
                //如果剩余大于0,进行减一
                if (slideFactory.getProductSurplus().addAndGet(-1) >= 0) {
                    //如果抢购成功，保存信息
                    slideFactory.getGetStores().get(slideFactory.getPublishNo()).put(storeId, "");
                    if (slideFactory.getProductSurplus().get() < 1) {
                        checkSlideFctoryShouldNotify();
                    }
                    return new RpsMsg().setStausCode(200).setMsg("恭喜你，抢到了，获得下轮发放开始前的轮播资格");
                } else {
                    //如果获取之后小于0，获取无效，尝试唤醒工厂
                    return checkSlideFctoryShouldNotify();
                }
            } else {
                return checkSlideFctoryShouldNotify();
            }
        } else {
            if (slideFactory.getState() == 0) {
                return new RpsMsg().setStausCode(300).setMsg("手速太快,还没有发放");
            } else {
                return new RpsMsg().setStausCode(300).setMsg("来晚了，活动停止");
            }
        }
    }

    /*检测是否应该唤醒工厂*/
    private RpsMsg checkSlideFctoryShouldNotify() {
        //如果已经被强完，尝试唤醒工厂线程继续生产
        if (slideFactory.getState() == 0 || slideFactory.getState() == -1) {
            //如果已经进入的状态或者已经关闭，则直接结束
            return new RpsMsg().setStausCode(300).setMsg("手速慢了,被枪光了");
        } else {
            //否则就还在发放状态，尝试上锁
//            if (slideFactory.getLock().tryLock()) {
//                //如果尝试上锁失败，再检测下工厂状态，如果没有改变再上锁
//                if (slideFactory.getState() == 0 || slideFactory.getState() == -1) {
//                    return new RpsMsg().setStausCode(300).setMsg("手速慢了,被枪光了");
//                } else {
//                    //说明通知没有到位
//                    slideFactory.getLock().lock();
//                    //如果上锁成功，再次检测工厂状态
//                    if (slideFactory.getState() == 0 || slideFactory.getState() == -1) {
//                        return new RpsMsg().setStausCode(300).setMsg("手速慢了,被枪光了");
//                    } else {
//                        //唤醒线程
//                        slideFactory.getFactoryCondition().signal();
//                        //并释放锁
//                        slideFactory.getLock().unlock();
//                        return new RpsMsg().setStausCode(300).setMsg("手速慢,被枪光了");
//                    }
//                }
//            } else {
            slideFactory.getLock().lock();
            //如果上锁成功，再次检测工厂状态
            if (slideFactory.getState() == 0 || slideFactory.getState() == -1) {
                return new RpsMsg().setStausCode(300).setMsg("手速慢了,被枪光了");
            } else {
                //唤醒线程
                slideFactory.getFactoryCondition().signal();
                //并释放锁
                slideFactory.getLock().unlock();
                return new RpsMsg().setStausCode(300).setMsg("手速慢了,被枪光了");
            }
        }
//        }
    }

    /*商家上传轮播图片*/
    @PostMapping("/slide/store/up")
    public RpsMsg postSlide(MultipartFile file, HttpSession session) {
        //判断当前商家是否具有资格
        if (slideFactory.getGetStores().isEmpty()) {
            //如果是获取者是空的
            return new RpsMsg().setStausCode(300).setMsg("活动关闭中，无法上传");
        }
        //判断是否有资格
        String storeId = (String) session.getAttribute("storeId");
        if (slideFactory.getGetStores().get(slideFactory.getPublishNo()).get(storeId) == null) {
            //如果没有
            return new RpsMsg().setStausCode(300).setMsg("抱歉，你没有得到本轮资格");
        } else if (!slideFactory.getGetStores().get(slideFactory.getPublishNo()).get(storeId).equals("")) {
            return new RpsMsg().setStausCode(300).setMsg("你已经上传过了");
        } else {
            //上传文件
            String fileName = String.valueOf(UUID.randomUUID()) + RandomUtil.randomString(2);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            CommonInputStreamResource commonInputStreamResource = null;
            try {
                commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
            map.add("path", SLIDEIMGPATH);
            map.add("file", commonInputStreamResource);
            map.add("fileName", fileName);
            HttpEntity httpEntity = new HttpEntity(map, new HttpHeaders());
            String newUrl = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class).getBody();
            if (newUrl == null) {
                return new RpsMsg().setMsg("上传失败").setStausCode(300);
            } else {
                //保存路径
                slideFactory.getGetStores().get(slideFactory.getPublishNo()).replace(storeId, "", newUrl);
                return new RpsMsg().setMsg("上传成功").setStausCode(200);
            }
        }
    }

    /*查询商家是否已经上传的图片*/
    @GetMapping("/slide/store/up/over")
    public RpsMsg getStoreUpOver(HttpSession session) {
        String storeId = (String) session.getAttribute("storeId");
        if (slideFactory.getGetStores().get(slideFactory.getPublishNo()).get(storeId) == null) {
            //如果没有
            return new RpsMsg().setStausCode(200).setData(false);
        } else if (slideFactory.getGetStores().get(slideFactory.getPublishNo()).get(storeId).equals("")) {
            return new RpsMsg().setStausCode(200).setData(false);
        } else {
            return new RpsMsg().setStausCode(200).setData(true);
        }
    }

    /*获取当前中奖名单*/
    @GetMapping("/slide/get/all")
    public RpsMsg getSlideAll() {
        Map<String, List<String>> stringListMap = new HashMap<>();
        if (slideFactory.getGetStores().isEmpty()) {
            //如果没有结果直接返回为空
            return new RpsMsg().setStausCode(200).setData(true);
        } else {
            //封装数据
            slideFactory.getGetStores().entrySet().stream().peek(
                    s -> {
                        List<String> stringList = new ArrayList<>();
                        if (!s.getValue().isEmpty()) {
                            //如果有数据
                            s.getValue().entrySet().stream().peek(m -> {
                                stringList.add(m.getKey());
                            }).count();
                        }
                        stringListMap.put(s.getKey(), stringList);
                    }
            ).count();
            //返回数据
            return new RpsMsg().setStausCode(200).setData(stringListMap);
        }
    }


    /*查询轮播图*/
    @GetMapping("/slide/consumer/get")
    public RpsMsg getSlideShow() {
        SlideShowMap slideShowMap = new SlideShowMap(new ArrayList<>(), 0);
        //如果工厂中有信息
        if (!slideFactory.getGetStores().isEmpty()) {
            //获取当前具有轮播资格的轮播图
            slideFactory.getGetStores().get(slideFactory.getPublishNo()).entrySet().stream()
                    .peek(
                            s -> {
                                if (s.getValue() != "") {
                                    slideShowMap.getSlideShows().add(new SlideShow(s.getKey(), s.getValue()));
                                }
                            }
                    ).count();
            if (slideShowMap.getSlideShows().size() == 0) {
                return new RpsMsg().setStausCode(200).setMsg("无数据");
            } else {
                //设置轮播时间
                slideShowMap.setSeconds((Math.abs(slideFactory.getNextPublishDate().getTime() - new Date().getTime())));
                return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(slideShowMap);
            }
        } else {
            return new RpsMsg().setStausCode(200).setMsg("无数据");
        }
    }

}
