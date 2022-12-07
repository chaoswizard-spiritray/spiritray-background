package spiritray.consumer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.History;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

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

    /*获取指定用户最近浏览时间最长的指定条数的未删除商品*/
    public List<String> selectLookRecentlyLongCommodityIdAndNoDelete(@Param("phone") long phone, @Param("num") int num);

    /*查询指定用户的指定商品浏览历史*/
    public History selectNoDeleteHisByPhoneAndCommodityId(@Param("phone") long phone, @Param("commodityId") String commodityId);

    /*删除指定用户的指定商品浏览历史*/
    public int updateIsDeleteHisByPhoneAndCommodityId(@Param("phone") long phone, @Param("commodityId") String commodityId);

    /*添加指定用户的指定商品浏览历史*/
    public int insertHisOne(@Param("lookHis") History history);

    /*修改指定用户的指定商品浏览历史*/
    public int updateHisByPhoneAndCommodityId(@Param("his") History history);

    /*查询指定用户近指定天数的数目*/
    public List<Map<String, Object>> selectCountDateByRecentDay(@Param("phone") long phone, @Param("recentDay") int recentDay, @Param("isDelete") int is_delete);

    /*批量查询指定时间指定条数的商品id*/
    public List<String> selectCommodityByDateAndNumMany(@Param("phone") long phone, @Param("params") Map<Date, Integer> params);
}