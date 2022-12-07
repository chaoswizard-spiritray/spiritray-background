package spiritray.seller.controller;

import cn.hutool.json.JSONUtil;
import netscape.javascript.JSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.Attribute;
import spiritray.common.pojo.PO.Category;
import spiritray.seller.mapper.CategoryMapper;
import spiritray.seller.mapper.CavMapper;
import spiritray.seller.service.CavService;
import spiritray.seller.service.SkuService;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * ClassName:CavController
 * Package:spiritray.seller.controller
 * Description:
 * 商品的属性、种类相关请求
 *
 * @Date:2022/4/26 13:30
 * @Author:灵@email
 */
@RestController
@RequestMapping("/cav")
public class CavController {
    @Autowired
    private CavService cavService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private CavMapper cavMapper;

    /*查询商品的种类*/
    @GetMapping("/category/{categoryId}")
    public RpsMsg getCategory(@PathVariable int categoryId) {
        return cavService.queryCategory(categoryId);
    }

    /*查询商品属性*/
    @GetMapping("/attribute/{categoryId}")
    public RpsMsg getAttribute(@PathVariable int categoryId) {
        return cavService.queryAttribute(categoryId);
    }

    /*批量添加商品属性*/
    @PostMapping("/attribute/add/many")
    public RpsMsg postAttribute(String attributes, long categoryId) {
        List<Attribute> attributes1 = JSONUtil.toList(attributes, Attribute.class);
        if (attributes1.stream().filter(s -> {
            return s.getAttributeName() == null || s.getAttributeName().trim().equals("");
        }).count() > 0) {
            return new RpsMsg().setStausCode(300).setMsg("存在空名");
        }
        int count = cavMapper.insertAttributes(categoryId, attributes1);
        return new RpsMsg().setStausCode(200).setMsg("插入成功" + count + "条," + "失败" + (attributes1.size() - count) + "条")
                .setData(attributes1.stream().filter(s -> {
                    return s.getAttributeId() != -1;
                }).collect(Collectors.toList()));
    }

    /*查询指定商品的单值属性值*/
    @GetMapping("/cav/simple/{commodityId}")
    public RpsMsg getSimpleCav(@PathVariable String commodityId) {
        return cavService.queryCavByCommodityId(commodityId, false);
    }

    /*查询商品多值属性值*/
    @GetMapping("/cav/mul/{commodityId}")
    public RpsMsg getMulCav(@PathVariable String commodityId) {
        RpsMsg data = cavService.queryCavByCommodityId(commodityId, true);
        return data;
    }

    /*添加指定父类、种类名的种类*/
    @PostMapping("/category/add")
    public RpsMsg addCategory(String cate) {
        Category category = JSONUtil.toBean(cate, Category.class);
        categoryMapper.insertCategory(category);
        return new RpsMsg().setMsg("添加成功").setData(category).setStausCode(200);
    }
}
