package spiritray.plant.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.plant.service.AddressService;

/**
 * ClassName:AddressConreoller
 * Package:spiritray.plant.controller
 * Description:
 *
 * @Date:2022/6/8 11:26
 * @Author:灵@email
 */
@RestController
@RequestMapping("/location")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /*查询省*/
    @GetMapping("/province")
    public RpsMsg getProvince() {
        return addressService.queryProvince();
    }

    /*查询市*/
    @GetMapping("/city/{provinceId}")
    public RpsMsg getCity(@PathVariable int provinceId) {
        return addressService.queryCity(provinceId);
    }

    /*查询区*/
    @GetMapping("/district/{cityId}")
    public RpsMsg getDistrict(@PathVariable int cityId) {
        return addressService.queryDistrict(cityId);
    }

    /*根据id查询省*/
    @GetMapping("/province/simple/{provinceId}")
    public RpsMsg getProvinceById(@PathVariable int provinceId) {
        return addressService.queryProvinceById(provinceId);
    }

    /*根据id查询市*/
    @GetMapping("/city/simple/{cityId}")
    public RpsMsg getCityById(@PathVariable int cityId) {
        return addressService.queryCityById(cityId);
    }

    /*根据id查询区*/
    @GetMapping("/district/simple/{districtId}")
    public RpsMsg getDistrictById(@PathVariable int districtId) {
        return addressService.queryDistrictById(districtId);
    }
}
