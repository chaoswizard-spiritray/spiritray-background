package spiritray.common.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import spiritray.common.factoryConfig.SlideFactoryConfig;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ClassName:SlideFactory
 * Package:spiritray.common.factory
 * Description:
 *
 * @Date:2022/12/1 23:37
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SlideFactory {
    private Lock lock = new ReentrantLock();//创建一把锁，因为工厂是单例的所以所有属性也是单例的
    private Condition factoryCondition=lock.newCondition();//获取工厂锁对象
    private Condition storeCondition=lock.newCondition();//获取店铺锁对象，用于单独唤醒工厂线程
    private int state=-1;//工厂状态-1关闭、0生产中、1发放中
    private Date nextPublishDate;//下一次发放日期，当状态为0时生效
    private int workNumber;//工厂生产次数
    private int idleNum;//生产时长
    private TimeUnit idleUnit;//生产时长时间单位
    private int productNum;//生产数量
    private AtomicInteger productSurplus=new AtomicInteger(0);//产品剩余量
    private String publishNo;//当前发放编号
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getStores = new ConcurrentHashMap<>();//每轮发放获得者

}
