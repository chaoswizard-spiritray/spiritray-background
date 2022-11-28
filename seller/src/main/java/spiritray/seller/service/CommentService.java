package spiritray.seller.service;

import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Comment;

import javax.servlet.http.HttpServletResponse;

/**
 * ClassName:CommentService
 * Package:spiritray.seller.service
 * Description:
 *
 * @Date:2022/11/11 11:04
 * @Author:灵@email
 */
public interface CommentService {
    /*添加评论*/
    public RpsMsg addComment(Comment comment, String jwt, HttpServletResponse response);

    /*获取指定商品id指定条件评论信息*/
    public RpsMsg queryCommentByCommodityIdAndTypeAndCount(String commodityId, int type, int pageIndex, int pageNum);

    /*获取指定商品的评论条数*/
    public RpsMsg queryCommentCountsByCommodityIdAndType(String commodityId, int type);
}
