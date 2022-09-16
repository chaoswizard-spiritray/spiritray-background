package spiritray.plant.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SlideShow;
import spiritray.common.pojo.DTO.SlideShowMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    /*查询轮播图*/
    @GetMapping("/slideshow")
    public RpsMsg getSlideShow() {
        SlideShowMap slideShowMap = null;
        //如果redis中存在这个键，就获取队首元素
        if (redisTemplate.hasKey("slideShows")) {
            try {
                //元素出队列
                slideShowMap = (SlideShowMap) JSONObject.parse((String) redisTemplate.opsForList().leftPop("slideShows"));
                if (slideShowMap == null) {
                    return new RpsMsg().setStausCode(200).setMsg("无数据");
                } else {
                    return new RpsMsg().setMsg("查询成功").setStausCode(200).setData(slideShowMap);
                }
            } catch (Exception e) {
                return new RpsMsg().setStausCode(300).setMsg("查询失败");
            }
        } else {
            return new RpsMsg().setStausCode(200).setMsg("无数据");
        }
    }

    /*添加轮播图信息*/
    @PostMapping("/slideshow")
    public RpsMsg addSlideShow(String slideShows, long timeSecond) {
        //检测信息的完整性
        if (slideShows == null || timeSecond <= 0) {
            return new RpsMsg().setStausCode(300).setMsg("添加失败");
        }
        //封装元素信息
        List<SlideShow> slideShowList = JSONObject.parseArray(slideShows).toJavaList(SlideShow.class);
        SlideShowMap slideShowMap = new SlideShowMap(slideShowList, timeSecond);
        //添加到slideShows队尾
        if (redisTemplate.opsForList().rightPush("slideShows", slideShowMap) > 0) {
            return new RpsMsg().setMsg("添加成功").setStausCode(200);
        } else {
            return new RpsMsg().setMsg("添加失败").setStausCode(300);
        }
    }
}
