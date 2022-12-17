package spiritray.seller.service.imp;

import cn.hutool.core.thread.ThreadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spiritray.common.pojo.BO.CheckOrderInfo;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.DTO.SSMap;
import spiritray.seller.mapper.CommodityMapper;
import spiritray.seller.mapper.SkuMapper;
import spiritray.seller.service.SkuService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

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
    @Autowired
    private CommodityMapper commodityMapper;

    @Autowired
    @Qualifier("skuLock")
    private Lock skuLock;//全局sku数量增减锁

    @Autowired
    @Qualifier("skuLockAddCondition")
    private Condition skuLockAddCondition;//全局sku数量增锁对象

    @Autowired
    @Qualifier("skuLockSubCondition")
    private Condition skuLockSubCondition;//全局sku数量减锁对象


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
        //先验证一次商品是否都在售
        List<String> ids = checkParams.stream().map(SSMap::getAttributeName).collect(Collectors.toList());
        if (commodityMapper.selectCountInSellByCommodityIds(ids) < ids.size()) {
            return new RpsMsg().setMsg("订单中商品已下架").setStausCode(300);
        }
        for (int i = 0; i < checkParams.size(); i++) {
            //先获取数量
            Integer num = skuMapper.selectSkuNumByCommodityAndSku(checkParams.get(i).getAttributeName(), checkParams.get(i).getAttributeValue());
            //第一次判断数量
            if (num == null || num <= 0) {
                throw new IllegalArgumentException();
            }
            if (nums.get(i) > (int) num) {
                throw new IllegalArgumentException();
            } else {
                //尝试获取锁
                skuLock.lock();
                num = skuMapper.selectSkuNumByCommodityAndSku(checkParams.get(i).getAttributeName(), checkParams.get(i).getAttributeValue());
                //再判断一次
                if (num == null || num <= 0) {
                    skuLock.unlock();
                    throw new IllegalArgumentException();
                }
                if (nums.get(i) > num) {
                    skuLock.unlock();
                    throw new IllegalArgumentException();
                } else {
                    //修改前再判断一次
                    if (commodityMapper.selectCountInSellByCommodityIds(ids) < ids.size()) {
                        return new RpsMsg().setMsg("订单中商品已下架").setStausCode(300);
                    }
                    //更新数目，能够保证数据不出问题，因为mysql默认的隔离级别是可重复读，在更新时myisam存储引擎使用的表锁，innodb使用的是行锁以及MVCC
                    skuMapper.updateSkuNumByCommodityAndSku(checkParams.get(i).getAttributeName(), checkParams.get(i).getAttributeValue(), nums.get(i), 1);
                }
                skuLock.unlock();
            }
        }
        return new RpsMsg().setStausCode(200).setMsg("商品数量减少成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RpsMsg addSkuNum(String commodityId, String skuValue, int num) {
        //先尝试获取锁
        if (skuLock.tryLock()) {
            //执行代码
            if (skuMapper.updateSkuNumByCommodityAndSku(commodityId, skuValue, num, 0) > 0) {
                skuLock.unlock();
                return new RpsMsg().setStausCode(200).setMsg("增加成功").setData(null);
            } else {
                skuLock.unlock();
                return new RpsMsg().setStausCode(300).setMsg("增加失败");
            }
        } else {
            //等待2秒重新尝试获取锁
            ThreadUtil.sleep(2, TimeUnit.SECONDS);
            if (skuLock.tryLock()) {
                //执行代码
                if (skuMapper.updateSkuNumByCommodityAndSku(commodityId, skuValue, num, 0) > 0) {
                    skuLock.unlock();
                    return new RpsMsg().setStausCode(200).setMsg("增加成功");
                } else {
                    skuLock.unlock();
                    return new RpsMsg().setStausCode(300).setMsg("增加失败");
                }
            } else {
                //如果还是没有获取到返回错误
                return new RpsMsg().setStausCode(300).setMsg("增加失败");
            }
        }
    }
}
