package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Comment;

import java.util.List;

/**
 * ClassName:CommentMapper
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/11/12 19:46
 * @Author:灵@email
 */
@Repository
public interface CommentMapper {
    /*插入单条评论信息*/
    public Integer insertComment(@Param("comment") Comment comment);

    /*统计评论条数指定商品id、类型*/
    public Integer selectCommentCountsByCommodityIdAndType(@Param("commodityId") String commodityId, @Param("type") Integer type);

    /*查询评论指定商品id、类型、分页参数*/
    public List<Comment> selectCommentByCommodityIdAndTypeAndPageSeaparate(@Param("commodityId") String commodityId, @Param("type") Integer type, @Param("pageNo") Integer pageNo, @Param("pageNum") Integer pageNum);
}
