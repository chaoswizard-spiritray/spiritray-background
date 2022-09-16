package spiritray.seller.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Cav;
import spiritray.seller.mapper.CavMapper;
import spiritray.seller.service.CavService;

import java.util.List;

/**
 * ClassName:CavServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 *
 * @Date:2022/4/26 13:53
 * @Author:灵@email
 */
@Service
public class CavServiceImp implements CavService {
    @Autowired
    private CavMapper cavMapper;


    @Override
    public RpsMsg queryCategory(int categoryId) {
        return new RpsMsg().setStausCode(200).setData(cavMapper.selectCategoryById(categoryId));
    }

    @Override
    public RpsMsg queryAttribute(int categoryId) {
        return new RpsMsg().setStausCode(200).setData(cavMapper.selectAttributeByCategoryId(categoryId));
    }

    @Override
    public RpsMsg queryCavByCommodityId(String commodityId, boolean isMul) {
        if (isMul) {
            return new RpsMsg().setData(cavMapper.selectCavByCommodityId(commodityId, 1)).setStausCode(200);
        } else {
            return new RpsMsg().setData(cavMapper.selectCavByCommodityId(commodityId, 0)).setStausCode(200);
        }
    }

    @Override
    public RpsMsg queryAllCavCommodityId(String commodityId) {
        return new RpsMsg().setStausCode(200).setData(cavMapper.selectAllCavCommodityId(commodityId));
    }

}
