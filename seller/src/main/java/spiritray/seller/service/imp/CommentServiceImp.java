package spiritray.seller.service.imp;

import cn.hutool.core.date.DateException;
import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import spiritray.common.pojo.DTO.CommodityComment;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.common.pojo.PO.Comment;
import spiritray.seller.mapper.CommentMapper;
import spiritray.seller.service.CommentService;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName:CommentServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 *
 * @Date:2022/11/11 11:05
 * @Author:灵@email
 */
@Service
public class CommentServiceImp implements CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private HttpHeaders headers;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private final String CONSUMER_URL = "http://localhost:8080";

    private final String SELLER_URL = "http://localhost:8081";

    private final String ORDER_URL = "http://localhost:8082";


    @SneakyThrows
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RpsMsg addComment(Comment comment, String jwt, HttpServletResponse response) {
        //先插入评论
        int row = commentMapper.insertComment(comment);
        if (row == 0) {
            return new RpsMsg().setStausCode(300).setMsg("发布失败");
        } else {
            //修改订单状态
            headers.add("jwt", jwt);
            MultiValueMap multiValueMap = new LinkedMultiValueMap();
            multiValueMap.add("orderNumber", comment.getOrderNumber());
            multiValueMap.add("odId", comment.getOdId());
            ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(SELLER_URL + "/order/state/publish", HttpMethod.PUT, new HttpEntity<>(multiValueMap, headers), RpsMsg.class);
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                //如果请求异常
                response.getWriter().write(JSONUtil.toJsonStr(new RpsMsg().setStausCode(300).setMsg("发布失败")));
                throw new DateException("修改发布状态失败");
            } else {
                //如果响应,注意可能需要进行json转换
                RpsMsg rpsMsg = responseEntity.getBody();
                if (rpsMsg != null && rpsMsg.getStausCode() == 200) {
                    return rpsMsg.setMsg("发布成功");
                } else {
                    response.getWriter().write(JSONUtil.toJsonStr(rpsMsg.setStausCode(300).setMsg("发布失败")));
                    throw new DateException("修改发布状态失败");
                }
            }
        }
    }

    @Override
    public RpsMsg queryCommentByCommodityIdAndTypeAndCount(String commodityId, int type, int pageIndex, int pageNum) {
        List<Comment> comments = commentMapper.selectCommentByCommodityIdAndTypeAndPageSeaparate(commodityId, type, pageIndex, pageNum);
        //如果评论数目为0，直接返回
        if (comments == null || comments.size() == 0) {
            return new RpsMsg().setMsg("查询成功").setStausCode(200);
        }
        //否则提取非匿名电话
        List<Long> phones = new ArrayList<Long>();
        for (Comment comment : comments) {
            if (comment.getIsAnonymous() == 0) {
                phones.add(comment.getConsumerPhone());
            }
        }
        MultiValueMap multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("phones", phones);
        //获取评论昵称、头像
        ResponseEntity<RpsMsg> responseEntity = restTemplate.exchange(CONSUMER_URL + "/consumer/headAndName/many", HttpMethod.GET, new HttpEntity<>(multiValueMap, headers), RpsMsg.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return new RpsMsg().setMsg("系统繁忙").setStausCode(300);
        }
        //提取数据
        Map<Long, SSMap> nickNameAndHead = (Map<Long, SSMap>) responseEntity.getBody().getData();
        //获取订单所选sku
        responseEntity = restTemplate.exchange(ORDER_URL, HttpMethod.GET, new HttpEntity<>(multiValueMap, headers), RpsMsg.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return new RpsMsg().setMsg("系统繁忙").setStausCode(300);
        }
        //提取数据
        Map<Long, String> skus = (Map<Long, String>) responseEntity.getBody().getData();
        //过滤并封装数据
        List<CommodityComment> commodityComments = new ArrayList<>();
        for (Comment comment : comments) {
            if (comment.getIsAnonymous() == 0) {
                commodityComments.add(
                        new CommodityComment()
                                .setAttchedMap(comment.getAttchedMap())
                                .setCommentContent(comment.getCommentContent())
                                .setCommentNo(comment.getCommentNo())
                                .setConsumerHead(nickNameAndHead.get(comment.getConsumerPhone()).getAttributeName())
                                .setConsumerNickname(nickNameAndHead.get(comment.getConsumerPhone()).getAttributeValue())
                                .setSkuValue(skus.get(comment.getConsumerPhone()))
                                .setStarLevel(comment.getStarLevel())
                                .setStartDate(comment.getStartDate())
                );
            } else {
                commodityComments.add(
                        new CommodityComment()
                                .setAttchedMap(comment.getAttchedMap())
                                .setCommentContent(comment.getCommentContent())
                                .setCommentNo(comment.getCommentNo())
                                .setConsumerHead("/static/comment/head/default.png")
                                .setConsumerNickname("匿名")
                                .setSkuValue(skus.get(comment.getConsumerPhone()))
                                .setStarLevel(comment.getStarLevel())
                                .setStartDate(comment.getStartDate())
                );
            }
        }
        //返回数据
        return new RpsMsg().setStausCode(200).setData(commodityComments).setMsg("查询成功");
    }

    @Override
    public RpsMsg queryCommentCountsByCommodityIdAndType(String commodityId, int type) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(commentMapper.selectCommentCountsByCommodityIdAndType(commodityId, type));
    }
}
