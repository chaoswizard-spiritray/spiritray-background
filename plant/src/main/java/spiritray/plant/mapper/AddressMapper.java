package spiritray.plant.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import spiritray.common.pojo.DTO.NSMap;

import java.util.List;

/**
 * ClassName:AddressMapper
 * Package:spiritray.plant.mapper
 * Description:
 *
 * @Date:2022/6/8 11:35
 * @Author:灵@email
 */
@Repository
@Mapper
public interface AddressMapper {

    /*查询省*/
    public List<NSMap> selectProvinceAll();

    /*查询市*/
    public List<NSMap> selectCityByProvinceId(@Param("provinceId") int provinceId);

    /*查询区*/
    public List<NSMap> selectDistrictByCityId(@Param("cityId") int cityId);

    /*查询省*/
    public NSMap selectProvinceSimple(@Param("provinceId") int provinceId);

    /*查询市*/
    public NSMap selectCitySimple(@Param("cityId") int cityId);

    /*查询区*/
    public NSMap selectDistrictSimple(@Param("disId") int disId);
}
