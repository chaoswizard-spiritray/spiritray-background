package spiritray.seller.controller;

import cn.hutool.json.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.FileUploadMsg;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.seller.mapper.CommodityDetailMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ClassName:CommodityDetailController
 * Package:spiritray.seller.controller
 * Description:
 *
 * @Date:2022/12/12 17:31
 * @Author:灵@email
 */
@RestController
@RequestMapping("/commodity/detail")
public class CommodityDetailController {
    @Autowired
    private CommodityDetailMapper commodityDetailMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    /*获取商品详情信息*/
    @GetMapping("/all/{commodityId}")
    public RpsMsg getCommodityDetail(@PathVariable String commodityId) {
        String detail = commodityDetailMapper.getCommodityDetail(commodityId);
        if (detail != null) {
            return new RpsMsg().setStausCode(200).setData(JSONUtil.toList(detail, String.class));
        }
        return new RpsMsg().setStausCode(200);
    }

    /*上传商品详情*/
    @SneakyThrows
    @PostMapping("/up")
    @Transactional(rollbackFor = Exception.class)
    public RpsMsg upCommodityShop(List<MultipartFile> files, String commodityId, String detail) {
        //先将非空文件进行上传，并更新到路径中
        List<String> details = JSONUtil.toList(detail, String.class);
        if (files != null)
            files = files.stream().filter(s -> s != null).collect(Collectors.toList());//得到不为空的文件
        //上传文件
        FileUploadMsg fileUploadMsg = null;//上传文件返回信息
        if (files != null && files.size() > 0) {
            for (int i = 0; i < files.size(); i++) {
                if (files.get(i) != null) {
                    String imgPath = "/static/store/detail";
                    RpsMsg rpsMsg = new RpsMsg();
                    //创建参数map
                    MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
                    //将每个文件进行创建输入文件流
                    for (MultipartFile file : files) {
                        CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
                        param.add("files", commonInputStreamResource);
                    }
                    param.add("path", imgPath);
                    param.add("isBack", false);
                    HttpEntity httpEntity = new HttpEntity(param, new HttpHeaders());
                    ResponseEntity<FileUploadMsg> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_MUL(), HttpMethod.POST, httpEntity, FileUploadMsg.class);
                    fileUploadMsg = responseEntity.getBody();
                }
            }
        }
        //更新detail
        if (fileUploadMsg != null) {
            int j = 0;
            for (int i = 0; i < details.size(); i++) {
                if (details.get(i).indexOf("blob:") > -1) {
                    //如果是新加文件,这是在文件全部上传成功的情况下，如果没有上传成功建议用单文件上传处理，但是效率比较低
                    details.set(i, fileUploadMsg.getFilePaths().get(j++));
                }
            }
        }
        //再获取上次的细节记录
        String lastDetailString = commodityDetailMapper.getCommodityDetail(commodityId);
        //保存当前细节
        int row = commodityDetailMapper.updateCommodityDetail(commodityId, JSONUtil.toJsonStr(details));
        //如果操作成功
        if (row == 1) {
            if (lastDetailString != null) {
                //如果已有记录就得到应该删除的图片
                List<String> lastDetails = JSONUtil.toList(lastDetailString, String.class);
                Set<String> shouldDelete = new HashSet<>(lastDetails);
                shouldDelete.removeAll(details);//得到差集
                shouldDelete.stream().peek(s -> {
                    MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                    map.add("path", s);
                    HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(map, new HttpHeaders());
                    ResponseEntity<Boolean> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, httpEntity, Boolean.class);
                }).count();
            }
            return new RpsMsg().setStausCode(200).setMsg("保存成功");
        } else {
            //删除上传的图片
            fileUploadMsg.getFilePaths().stream().peek(s -> {
                MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                map.add("path", s);
                HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(map, new HttpHeaders());
                ResponseEntity<Boolean> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, httpEntity, Boolean.class);
            }).count();
            return new RpsMsg().setStausCode(300).setMsg("保存失败，稍后再试");
        }
    }
}
