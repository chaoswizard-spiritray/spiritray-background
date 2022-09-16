package spiritray.seller.service.imp;

import cn.hutool.core.util.RandomUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.CommonInputStreamResource;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.FileUploadMsg;
import spiritray.common.pojo.DTO.RpsMsg;
import spiritray.common.pojo.PO.SellerAccount;
import spiritray.common.pojo.PO.Store;
import spiritray.common.tool.CheckResponseInfTool;
import spiritray.seller.mapper.StoreMapper;
import spiritray.seller.service.StoreService;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;

/**
 * ClassName:StoreServiceImp
 * Package:spiritray.seller.service.imp
 * Description:
 *
 * @Date:2022/4/21 15:45
 * @Author:灵@email
 */
@Service
public class StoreServiceImp implements StoreService {
    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FileUploadInterface fileUploadInterface;

    @Autowired
    private HttpHeaders httpHeaders;

    @Override
    public RpsMsg findStoreByPhone(long phone, HttpSession session) {
        Store store = storeMapper.selectStoreByPhone((phone));
        if (store != null) {
            session.setAttribute("storeId", store.getStoreId());
            session.setAttribute("storeStatus", store.getStatus());
            return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(store);
        } else {
            return new RpsMsg().setStausCode(300).setMsg("没有相关信息");
        }
    }

    @Override
    public RpsMsg findStoreByStoreId(String storeId) {
        Store store = storeMapper.selectStoreByStoreId(storeId);
        if (store == null) {
            return new RpsMsg().setStausCode(300).setMsg("没有相关信息");
        } else {
            return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(store);
        }
    }

    @Override
    public RpsMsg queryCloseInf(String storeId) {
        Store store = storeMapper.selectCloseInfById(storeId);
        if (store == null) {
            return new RpsMsg().setMsg("没有相关信息").setStausCode(300);
        } else {
            return new RpsMsg().setMsg("查询成功").setStausCode(200).setData(store);
        }
    }

    @Override
    public RpsMsg queryLicenseByStoreId(String storeId) {
        List<String> urls = storeMapper.selectLicenseByStoreId(storeId);
        if (urls == null) {
            return new RpsMsg().setMsg("没有相关信息").setStausCode(200);
        } else {
            return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(urls);
        }
    }

    @SneakyThrows
    @Override
    public RpsMsg addLicense(List<MultipartFile> files, String storeId) {
        String imgPath = "/static/store/license";
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
        HttpEntity httpEntity = new HttpEntity(param, httpHeaders);
        ResponseEntity<FileUploadMsg> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_MUL(), HttpMethod.POST, httpEntity, FileUploadMsg.class);
        if (responseEntity.getBody() == null) {
            return rpsMsg.setMsg("上传失败,请稍后再试").setStausCode(300);
        }
        FileUploadMsg fileUploadMsg = responseEntity.getBody();
        //将信息存储到数据库
        storeMapper.insertLicenseByStoreId(fileUploadMsg.getFilePaths(), storeId);
        return rpsMsg.setStausCode(200).setMsg("上传成功:" + fileUploadMsg.getSuccessNum() + "条，失败:" + fileUploadMsg.getFaileNum() + "条")
                .setData(fileUploadMsg.getFilePaths());
    }

    @Override
    public RpsMsg queryAccountsByStoreId(String storeId, int accaId) {
        return new RpsMsg().setStausCode(200).setMsg("查询成功").setData(storeMapper.selectSellerAccountByStoreId(storeId, accaId));
    }

    @Transactional
    @Override
    public RpsMsg addAccount(SellerAccount account) {
        if (storeMapper.insertAccount(account) >= 0) {
            return new RpsMsg().setStausCode(200).setMsg("添加成功");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("添加失败");
        }
    }

    @Transactional
    @Override
    public RpsMsg modifyAccount(SellerAccount account) {
        if (storeMapper.updateAccount(account) >= 0) {
            return new RpsMsg().setStausCode(200).setMsg("修改成功");
        } else {
            return new RpsMsg().setStausCode(300).setMsg("修改失败");
        }

    }

    @Override
    public RpsMsg removeAccount(int accountId, String storeId) {
        if (storeMapper.deleteAccount(accountId, storeId) > 0) {
            return new RpsMsg().setMsg("删除成功").setStausCode(200);
        } else {
            return new RpsMsg().setMsg("删除失败").setStausCode(300);
        }
    }

    @Override
    public RpsMsg queryCollectionAccountByStoreIdAndAccaId(String storeId, int accaId) {
        return new RpsMsg().setMsg("查询成功").setStausCode(200).setData(storeMapper.selectAccountCollectionByStoreIdAndAccaId(storeId, accaId));
    }

    @SneakyThrows
    @Override
    public RpsMsg modifyStore(Store store, MultipartFile file) {
        //如果文件不为空,我们就查询出原来的图像进行覆盖
        if (file != null) {
            String imgUrl = storeMapper.selectStoreHeadById(store.getStoreId());
            if (imgUrl.indexOf("default") < 0) {
                //如果不是默认头像，就先删除先前的头像
                MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
                map.add("path", imgUrl);
                HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity(map, new HttpHeaders());
                ResponseEntity<Boolean> responseEntity = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.DELETE, httpEntity, Boolean.class);
                if (!responseEntity.getBody()) {
                    //如果删除失败直接返回失败信息
                    return new RpsMsg().setMsg("修改头像失败").setStausCode(300);
                }
            }
            //封装上传单个文件参数
            String url = "/static/store/head";
            String fileName = String.valueOf(UUID.randomUUID()) + RandomUtil.randomString(2);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            CommonInputStreamResource commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(), file.getSize(), file.getOriginalFilename());
            map.add("path", url);
            map.add("file", commonInputStreamResource);
            map.add("fileName", fileName);
            HttpEntity httpEntity = new HttpEntity(map, httpHeaders);
            String newUrl = restTemplate.exchange(fileUploadInterface.getFILE_UPLOAD_SIMPLE(), HttpMethod.POST, httpEntity, String.class).getBody();
            if (newUrl == null) {
                return new RpsMsg().setMsg("头像修改失败").setStausCode(300);
            }
            store.setStoreHead(newUrl);
        }
        //更新数据
        return CheckResponseInfTool.checkCodeAndReturnOften(storeMapper.updateStore(store));
    }

    @Override
    public RpsMsg modifyStoreStatusByStoreId(String storeId, int status, String causeInf) {
        return CheckResponseInfTool.checkCodeAndReturnOften(storeMapper.updateStoreStatusByStoreId(storeId, status, causeInf));
    }


}
