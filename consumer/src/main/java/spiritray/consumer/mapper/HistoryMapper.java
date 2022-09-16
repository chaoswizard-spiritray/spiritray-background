package spiritray.consumer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.History;

import java.util.List;

/**
 * ClassName:HistoryMapper
 * Package:spiritray.consumer.mapper
 * Description:
 *
 * @Date:2022/6/15 15:05
 * @Author:灵@email
 */
@Mapper
@Repository
public interface HistoryMapper {

    /*获取指定用户最近浏览时间最长的指定条数商品*/
    public List<String> selectLookRecentlyLong(@Param("phone") long phone, @Param("num") int num);

    /*查询指定用户的指定商品浏览历史*/
    public History selectHisByPhoneAndCommodityId(@Param("phone") long phone, @Param("commodityId") String commodityId);

    /*删除指定用户的指定商品浏览历史*/
    public int deleteHisByPhoneAndCommodityId(@Param("phone") long phone, @Param("commodityId") String commodityId);

    /*添加指定用户的指定商品浏览历史*/
    public int insertHisOne(@Param("lookHis") History history);

    /*修改指定用户的指定商品浏览历史*/
    public int updateHisByPhoneAndCommodityId(@Param("his") History history);

}
