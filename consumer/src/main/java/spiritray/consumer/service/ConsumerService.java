package spiritray.consumer.service;

import spiritray.common.pojo.BO.MsgCode;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Address;
import spiritray.common.pojo.PO.Consumer;

/**
 * ClassName:ConsumerService
 * Package:spriritray.consumer.service
 * Description:
 * 买家个人信息服务
 *
 * @Date:2022/4/14 8:30
 * @Author:灵@email
 */
public interface ConsumerService {
    /*注册业务*/
    public RpsMsg register(MsgCode backCode, String code, Consumer consumer);

    /*登录*/
    public RpsMsg login(MsgCode backCode, String code, Consumer consumer);

    /*查询买家信息*/
    public RpsMsg queryConsumerByConsumer(Consumer consumer);

    /*修改买家个人信息*/
    public RpsMsg modifyConsumer(Consumer consumer);

    /*查询买家的收货地址*/
    public RpsMsg queryAddress(long phone);

    /*修改买家收货地址*/
    public RpsMsg modifyAddressById(Address address);

    /*添加收货地址*/
    public RpsMsg addAddress(Address address);

    /*删除收货地址*/
    public RpsMsg removeAddress(String addressId,long phone);

}
