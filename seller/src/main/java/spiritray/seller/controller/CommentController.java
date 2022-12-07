package spiritray.seller.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.FileUploadMsg;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Comment;
import spiritray.seller.mapper.CommentMapper;
import spiritray.seller.service.CommentService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

/**
 * ClassName:Comment
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/11/11 11:03
 * @Author:灵@email
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    /*添加单条商品评论信息*/
    @SneakyThrows
    @PostMapping("/one")
    public RpsMsg postOneComment(String comment, MultipartFile file, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        Comment comment1 = JSONUtil.toBean(comment, Comment.class);
        comment1.setCommentNo(String.valueOf(UUID.randomUUID())).setConsumerPhone((Long) session.getAttribute("phone")).setStartDate(new Timestamp(new Date().getTime()));
        if (file != null) {
            //上传图片
            String url = "/static/comment";
            String fileName = String.valueOf(UUID.randomUUID()) + RandomUtil.randomString(2);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
            map.add("path", url);
            map.add("file", commonInputStreamResource);
            map.add("fileName", fileName);
            HttpEntity httpEntity = new HttpEntity(map, new HttpHeaders());
            String newUrl = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class).getBody();
            if (newUrl == null) {
                return new RpsMsg().setMsg("发布失败").setStausCode(300);
            }
            comment1.setAttchedMap(newUrl);
        }
        try {
            return commentService.addComment(comment1, request.getHeader("jwt"), response);
        } catch (Exception e) {
            //删除图片
            MultiValueMap multiValueMap = new LinkedMultiValueMap();
            multiValueMap.set("path", comment1.getAttchedMap());
            restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, new HttpEntity<>(multiValueMap, new HttpHeaders()), String.class).getBody();
            return new RpsMsg().setStausCode(300).setMsg("系统繁忙");
        }
    }

    /*获取指定商品指定类型的评论条数
     * 0:统计所有评论
     * 1:统计最近评论条数，
     * 2:好评数
     * 3:差评数
     * 4:有图数
     * 5:多次购买
     * */
    @GetMapping("/counts/{commodityId}/{type}")
    public RpsMsg getCommentByCommodityIdAndType(@PathVariable String commodityId, @PathVariable int type) {
        return commentService.queryCommentCountsByCommodityIdAndType(commodityId, type);
    }

    /*获取指定商品指定类型、指定分页的评论*/
    @GetMapping("/{commodityId}/{type}/{pageNo}/{pageNum}")
    public RpsMsg getCommentByCommodityIdAndTypeAndCount(@PathVariable String commodityId, @PathVariable int type, @PathVariable int pageNo, @PathVariable int pageNum) {
        return commentService.queryCommentByCommodityIdAndTypeAndCount(commodityId, type, pageNo*pageNum, pageNum);
    }

    /*获取指定用户的订单评价的数目*/
    @GetMapping("/count/consumer/phone")
    public RpsMsg getCommentCountByConsumerPhone(HttpSession session) {
        return new RpsMsg().setData(commentMapper.selectCommentCountByConsumerPhone((Long) session.getAttribute("phone"))).setStausCode(200);
    }

    /*获取指定用户的评论信息*/
    @GetMapping("/consumer/phone")
    public RpsMsg getConsumerAllComment(HttpSession session) {
        return commentService.queryCommentByConsumerPhone((Long) session.getAttribute("phone"));
    }

}
