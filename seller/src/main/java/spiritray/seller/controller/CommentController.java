package spiritray.seller.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Comment;
import spiritray.seller.service.CommentService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

    /*添加单条商品评论信息*/
    @PostMapping("/one")
    public RpsMsg postOneComment(Comment comment, HttpSession session, HttpServletResponse response, HttpServletRequest request) {
        comment.setConsumerPhone((Long) session.getAttribute("phone"));
        return commentService.addComment(comment, request.getHeader("jwt"), response);
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
        return commentService.queryCommentByCommodityIdAndTypeAndCount(commodityId, type, pageNo, pageNum);
    }
}
