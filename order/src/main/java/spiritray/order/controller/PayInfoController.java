package spiritray.order.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.order.mapper.CpiMapper;
import spiritray.order.mapper.PbiMapper;
import spiritray.order.mapper.PtsMapper;

/**
 * ClassName:PayInfoController
 * Package:spiritray.order.controller
 * Description:
 *
 * @Date:2022/11/24 14:19
 * @Author:灵@email
 */
@RestController
@RequestMapping("/payinfo")
public class PayInfoController {
    @Autowired
    private CpiMapper cpiMapper;

    @Autowired
    private PbiMapper pbiMapper;

    @Autowired
    private PtsMapper ptsMapper;

    /*获取指定订单细节记录的cpi信息*/
    @GetMapping("/cpi/{orderNumber}")
    public RpsMsg getCpiByorderNumber(@PathVariable String orderNumber) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(cpiMapper.selectCpiByCpiId(orderNumber));
    }

    /*获取指定订单细节记录的pts信息*/
    @GetMapping("/pts/{orderNumber}")
    public RpsMsg getPtsByorderNumber(@PathVariable String orderNumber) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(ptsMapper.selectPtsByOrderNumber(orderNumber));
    }


}
