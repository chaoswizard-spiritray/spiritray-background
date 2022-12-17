package spiritray.seller.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.HotWord;

import java.util.Set;

/**
 * ClassName:HotWord
 * Package:spiritray.seller.mapper
 * Description:
 *
 * @Date:2022/12/10 19:23
 * @Author:灵@email
 */
@Mapper
@Repository
public interface HotWordMapper {
    /*批量插入数据*/
    public int insertHotWords(@Param("hotWords") Set<HotWord> hotWords);
}
