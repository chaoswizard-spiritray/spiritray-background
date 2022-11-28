package spiritray.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.websocket.Session;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://blog.csdn.net/fubo1990/article/details/79648766
 * ClassName:WebSocketConfig
 * Package:spiritray.common.config
 * Description:
 * https://blog.csdn.net/goodjava2007/article/details/126961787
 * Spring在启动时能够把BEAN(JAVA组件)注册到ioc容器里，实现控制反转，开发人员在使用Spring开发应用时，是看不到new关键字的，所有对象都应该从容器里获得，它们的生命周期在放入容器时已经确定。@Bean注解就是把我们将要实例化的对象转化成一个Bean，放在Spring容器中，等我们使用时，就会和@Autowired、@Resource配合到使用拿到该实例。注册BEAN的方法有@ComponentScan、@Bean、@Import、@Component、@Repository、@ Controller、@Service 、 @Configration等等。
 * SpringBoot - 向容器中注册组件的方法有哪些？
 * SpringBoot - @Configuration注解使用详解
 * <p>
 * SpringBoot 推荐使用JAVA配置的方式来完全代替XML配置，JAVA配置是通过 @Configration 和 @Bean 注解实现的。
 * （1）@Configration 注解：用于声明当前类是一个配置类，相当于Spring中的一个 XML 文件；相当XML文件中的标签
 * （2）@Bean 注解：作用在方法上，声明当前方法的返回值是一个Bean对象；相当XML文件中的标签。
 * <p>
 * 注解作用
 *
 * @Configuration与@Bean都是来自Spring的注解，作用是使用类来代替XML配置文件的功能，将一个POJO对象注入到Spring容器中。 基本概念
 * Spring的@Bean注解标注在方法上，用于告诉方法去产生一个Bean对象，然后把这个Bean对象交给Spring容器来管理，产生这个Bean对象的方法Spring只会调用一次。可以使用@Autowired或者@Resource注解获取Bean。(通过byTYPE方式（@Autowired）、byNAME方式（@Resource）)
 * <p>
 * 使用说明
 * 注解@Bean被声明在方法上，该方法都需要有一个返回类型，这个方法的返回类型就是注册到IOC容器中的类型，接口和类都是可以作为返回类型，介于面向接口原则，提倡返回类型为接口。该注解主要用在@Configuration注解的类的方法上，也可以用在@Component注解的类的方法上，添加的bean的id为方法名。
 * <p>
 * 使用要点
 * （1）@Bean 注解：作用在方法上，方法都需要有一个返回类型；
 * （2）@Bean 注解：用于表示当前方法返回一个 Spring 容器管理的 Bean；
 * （3）@Bean 的默认的名字和方法名一致(一般Bean都是首字母小写，因为方法名的首字母一般都是小写的)；
 * （4）@Bean 注解：一般和 @Component 或者 @Configuration 一起使用；
 * （5）@Bean 注解：默认作用域为单例作用域，可通过 @Scope(“prototype”) 设置为原型作用域；
 * （6）@Bean 注解：可以接受一个 String 数组来设置多个别名；
 * （7）@Configration 注解类中可以声明多个 @Bean 的方法，并且声明的 Bean 与 Bean 之间是可以有依赖关系的；
 * <p>
 * 组合使用
 * @Bean 注解常常与 @Scope、@Lazy，@DependsOn 和 @link Primary 注解一起使用：
 * （1）@Profile 注解：为不同环境下不同的配置提供了支持，如开发和生产环境的数配置是不同的；
 * （2）@Scope 注解：将 Bean 的作用域从单例改变为指定的作用域；
 * （3）@Lazy 注解：只有在默认单例作用域的情况下才有实际效果；
 * （4）@DependsOn 注解：表示在当前 Bean 创建之前需要先创建特定的其他 Bean的场景时，添加该注解。
 */
@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }


    /*买家websocket与映射关系表*/
    @Bean("consumerPWMap")
    public ConcurrentHashMap<Long, Session> getConsumerPWMap() {
        return new ConcurrentHashMap();
    }

    /*商家websocket与映射关系表*/
    @Bean("sellerPWMap")
    public ConcurrentHashMap<Long, Session> getSellerPWMap() {
        return new ConcurrentHashMap();
    }

    /*平台websocket与映射关系表*/
    @Bean("plantPWMap")
    public ConcurrentHashMap<Long, Session> getPlantPWMap() {
        return new ConcurrentHashMap();
    }

    /*处于消息标签页的买家*/
    @Bean("stayMsgHomeConsumer")
    public ConcurrentHashMap<Long, Session> getStayMsgHomeConsumer() {
        return new ConcurrentHashMap();
    }


    /*处于消息标签页的商家*/
    @Bean("stayMsgHomeSeller")
    public ConcurrentHashMap<Long, Session> getStayMsgHomeSeller() {
        return new ConcurrentHashMap();
    }

    /*处于消息标签页的平台*/
    @Bean("stayMsgHomePlant")
    public ConcurrentHashMap<Long, Session> getStayMsgHomePlant() {
        return new ConcurrentHashMap();
    }

    /*处于消息实时对话的的买家*/
    @Bean("stayMsgDetailConsumer")
    public ConcurrentHashMap<Long, Session> getStayMsgDetailConsumer() {
        return new ConcurrentHashMap();
    }


    /*处于消息实时对话的商家*/
    @Bean("stayMsgDetailSeller")
    public ConcurrentHashMap<Long, Session> getStayMsgDetailSeller() {
        return new ConcurrentHashMap();
    }

    /*处于消息实时对话的平台*/
    @Bean("stayMsgDetailPlant")
    public ConcurrentHashMap<Long, Session> getStayMsgDetailPlant() {
        return new ConcurrentHashMap();
    }

}
