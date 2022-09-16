package spiritray.plant.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.plant.mapper.AddressMapper;
import spiritray.plant.service.AddressService;

/**
 * ClassName:AddressServiceImp
 * Package:spiritray.plant.service
 * Description:
 *
 * @Date:2022/6/8 11:34
 * @Author:灵@email
 */
@Service
public class AddressServiceImp implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public RpsMsg queryProvince() {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(addressMapper.selectProvinceAll());
    }

    @Override
    public RpsMsg queryCity(int provinceId) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(addressMapper.selectCityByProvinceId(provinceId));
    }

    @Override
    public RpsMsg queryDistrict(int cityId) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(addressMapper.selectDistrictByCityId(cityId));
    }

    @Override
    public RpsMsg queryProvinceById(int proId) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(addressMapper.selectProvinceSimple(proId));
    }

    @Override
    public RpsMsg queryCityById(int cityId) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(addressMapper.selectCitySimple(cityId));
    }

    @Override
    public RpsMsg queryDistrictById(int disId) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(addressMapper.selectDistrictSimple(disId));
    }
}
