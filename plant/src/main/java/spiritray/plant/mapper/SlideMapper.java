package spiritray.plant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.PO.Slide;

import java.util.List;

/**
 * ClassName:SlideMapper
 * Package:spiritray.plant.mapper
 * Description:
 *
 * @Date:2022/12/2 14:34
 * @Author:灵@email
 */
@Repository
@Mapper
public interface SlideMapper {
    public int insertSlides(@Param("slides") List<Slide> slides);
}
