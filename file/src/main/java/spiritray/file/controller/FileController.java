package spiritray.file.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spiritray.common.pojo.BO.FileUploadInterface;
import spiritray.common.pojo.DTO.FileUploadMsg;
import spiritray.common.tool.FileUpLoadTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:UploadFile
 * Package:spiritray.file.controller
 * Description:
 *
 * @Date:2022/5/26 14:25
 * @Author:灵@email
 */
@RestController
@RequestMapping("/file")
public class FileController {
    /*单文件上传*/
    @PostMapping("/simple")
    public String saveSimple(MultipartFile file, String path, String fileName) {
        //调用文件保存
        try {
            return FileUpLoadTool.saveHttpImg(file, path, fileName);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /*多文件上传*/
    @PostMapping("/mul")
    public FileUploadMsg saveMul(List<MultipartFile> files, String path, boolean isBack) {
        FileUploadMsg fileUploadMsg = new FileUploadMsg();
        List<String> filePaths = new ArrayList<>();
        int success = 0;
        //循环保存，如果保存失败就存空，计数不加。如果出现异常就返回当前保存的值
        for (MultipartFile file : files) {
            String s = null;
            try {
                s = FileUpLoadTool.saveHttpImg(file, path, null);
                //如果路径为空，说明有异常
                if (s != null) {
                    filePaths.add(s);
                    success++;
                } else {
                    //如果需要回滚，就回滚
                    if (isBack) {
                        backDelete(filePaths);
                        return null;
                    }
                }
            } catch (FileNotFoundException e) {
                //出现异常时，如果需要删除原来的上传的文件就删除
                if (isBack) {
                    backDelete(filePaths);
                    return null;
                } else {
                    return fileUploadMsg.setFilePaths(filePaths).setSuccessNum(success).setFaileNum(files.size() - success);
                }
            }
        }
        return fileUploadMsg.setFilePaths(filePaths).setSuccessNum(success).setFaileNum(files.size() - success);
    }

    /*删除指定指定文件*/
    @DeleteMapping("/simple")
    public boolean deleteFile(String path) {
        return FileUpLoadTool.removeLocalFile(path);
    }

    /*回滚删除已经保存的文件*/
    private void backDelete(List<String> filePaths) {
        for (String filePath : filePaths) {
            FileUpLoadTool.removeLocalFile(filePath);
        }
    }
}
