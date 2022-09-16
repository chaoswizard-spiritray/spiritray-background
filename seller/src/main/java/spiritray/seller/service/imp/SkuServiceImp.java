package spiritray.seller.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spiritray.common.pojo.BO.CheckOrderInfo;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.seller.mapper.SkuMapper;
import spiritray.seller.service.SkuService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName:SkuServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 *
 * @Date:2022/6/14 0:03
 * @Author:灵@email
 */
@Service
public class SkuServiceImp implements SkuService {
    @Autowired
    private SkuMapper skuMapper;

    private Lock lock = new ReentrantLock();//全局锁


    @Override
    public RpsMsg queryCommoditySku(String commodityId) {
        return new RpsMsg().setData(skuMapper.selectSkuByCommodityId(commodityId)).setMsg("查询成功").setStausCode(200);
    }

    @Override
    public RpsMsg queryCheckOrderInfo(List<SSMap> checkParams) {
        List<CheckOrderInfo> checkOrderInfos = new ArrayList<>();
        for (int i = 0; i < checkParams.size(); i++) {
            CheckOrderInfo checkOrderInfo = skuMapper.selectCheckOrderInfoByCommodityId(checkParams.get(i));
            if (checkOrderInfo != null) {
                checkOrderInfos.add(checkOrderInfo);
            }
        }
        return new RpsMsg().setStausCode(200).setData(checkOrderInfos);
    }

    @Transactional(rollbackFor = IllegalArgumentException.class)
    @Override
    public RpsMsg updateSkuNum(List<SSMap> checkParams, List<Integer> nums) throws IllegalArgumentException {
        for (int i = 0; i < checkParams.size(); i++) {
            //先获取数量
            Integer num = skuMapper.selectSkuNumByCommodityAndSku(checkParams.get(i).getAttributeName(), checkParams.get(i).getAttributeValue());
            //第一次判断数量
            if (num == null) {
                throw new IllegalArgumentException();
            }
            if (nums.get(i) > (int) num) {
                throw new IllegalArgumentException();
            } else {
                //获取锁
                lock.lock();
                num = skuMapper.selectSkuNumByCommodityAndSku(checkParams.get(i).getAttributeName(), checkParams.get(i).getAttributeValue());
                //再判断一次
                if (num == null) {
                    lock.unlock();
                    throw new IllegalArgumentException();
                }
                if (nums.get(i) > num) {
                    lock.unlock();
                    throw new IllegalArgumentException();
                } else {
                    //更新数目，能够保证数据不出问题，因为mysql默认的隔离级别是可重复读，在更新时myisam存储引擎使用的表锁，innodb使用的是行锁以及MVCC
                    skuMapper.updateSkuNumByCommodityAndSku(checkParams.get(i).getAttributeName(), checkParams.get(i).getAttributeValue(), nums.get(i), 1);
                }
                lock.unlock();
            }
        }
        return new RpsMsg().setStausCode(200).setMsg("商品数量减少成功");
    }

    @Override
    @Transactional
    public RpsMsg addSkuNum(String commodityId, String skuValue, int num) {
        if (skuMapper.updateSkuNumByCommodityAndSku(commodityId, skuValue, num, 0) > 0) {
            return new RpsMsg().setStausCode(200).setMsg("增加成功");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("增加失败");
        }
    }
}
