package spiritray.plant.service;

import spiritray.common.pojo.DTO.RpsMsg;

/**
 * ClassName:AddressService
 * Package:spiritray.plant.service
 * Description:
 *
 * @Date:2022/6/8 11:30
 * @Author:灵@email
 */
public interface AddressService {

    /*获取省*/
    public RpsMsg queryProvince();

    /*获取市*/
    public RpsMsg queryCity(int provinceId);

    /*获取区*/
    public RpsMsg queryDistrict(int cityId);

    /*获取指定省*/
    public RpsMsg queryProvinceById(int proId);

    /*获取指定市*/
    public RpsMsg queryCityById(int cityId);

    /*获取指定区*/
    public RpsMsg queryDistrictById(int disId);
}
