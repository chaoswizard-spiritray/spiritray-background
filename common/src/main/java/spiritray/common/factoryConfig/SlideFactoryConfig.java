package spiritray.common.factoryConfig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

/**
 * ClassName:SlideFactoryConfig
 * Package:spiritray.common.factoryConfig
 * Description:
 *
 * @Date:2022/12/1 23:38
 * @Author:灵@email
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SlideFactoryConfig {
    private boolean startNow;//立即启动
    private int delayNum;//延后启动时间
    private TimeUnit delayTimeUnit;//延后启动时间单位
    private int workNumber;//工厂工作次数
    private int idleNum;//工作一次后空闲时间
    private TimeUnit idleTimeUnit;//空闲时间单位
    private int productSlideNum;//工厂容量
}
