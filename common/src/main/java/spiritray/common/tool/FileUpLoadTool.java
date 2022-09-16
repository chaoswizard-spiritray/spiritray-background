package spiritray.common.tool;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * ClassName:FileUpLoadTool
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/4/18 15:12
 * @Author:灵@email
 */
@Slf4j
public class FileUpLoadTool {
    /*保存本地用户不可见图像文件*/
    public static String saveImg(MultipartFile file, String imgPath, String fileName) throws FileNotFoundException {
        File file1 = new File(imgPath);
        //如果目录 不存在就创建级联目录
        if (!file1.exists()) {
            file1.mkdirs();
        }
        //判断是否传入文件名，如果传了就不使用随机生辰文件名
        if (fileName == null) {
            fileName = UUID.randomUUID() + "";//生成文件名
        }
        String type = file.getOriginalFilename().split("[.]")[1];//获取图片实际类型
        try {
            File file2 = new File(file1.getPath() + File.separator + fileName + "." + type);
            file.transferTo(file2);
            return file2.getPath();
        } catch (IOException e) {
            return null;

        }
    }

    /*保存用户可访问图片*/
    public static String saveHttpImg(MultipartFile file, String imgPath, String fileName) throws FileNotFoundException {
        File file1 = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + imgPath);
        //如果目录 不存在就创建级联目录
        if (!file1.exists()) {
            file1.mkdirs();
        }
        //判断是否传入文件名，如果传了就不使用随机生辰文件名
        if (fileName == null) {
            fileName = UUID.randomUUID() + "";//生成文件名
        }
        String type = file.getOriginalFilename().split("[.]")[1];//获取图片实际类型
        try {
            File file2 = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + imgPath + File.separator + fileName + "." + type);
            file.transferTo(file2);
            return imgPath + File.separator + fileName + "." + type;//返回文件http路径
        } catch (IOException e) {
            return null;
        }
    }

    /*删除指定本地项目中文件*/
    public static boolean removeLocalFile(String path) {
        File file = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + path);
        if (file != null && file.isFile()) {
            return FileSystemUtils.deleteRecursively(file);
        } else {
            return true;
        }
    }

    /*覆盖本地文件*/
    public static boolean coverLocalFile(String path, MultipartFile file) {
        File file1 = new File(path);
        if (file != null && file1.exists()) {
            try {
                file1.delete();
                file.transferTo(file1);
                return true;
            } catch (IOException e) {
                log.error(e.getClass() + "--" + e.getCause() + "::" + e.getMessage());
                return false;
            }
        } else {
            //如果文件为空，直接返回
            if (file == null) {
                return false;
            }
            //如果是原文件丢失，就新建
            String[] strings = path.split("(/[\\w||-]+)[.]");
            File dir = new File(strings[0]);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            try {
                file.transferTo(new File(path.replaceAll(strings[1], "") + file.getOriginalFilename().split("[.]")[1]));
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    /*覆盖http路径的图片*/
    @SneakyThrows
    public static boolean coverHttpFile(String url, MultipartFile file) {
        String classpath = String.valueOf(ClassUtils.getDefaultClassLoader().getResource("").getPath());
        //如果文件存在就先删除
        File file1 = new File(classpath + url);
        if (file1.exists() && file != null) {
            file1.delete();
        }
        if (file == null) {
            return false;
        }
        //新建文件并写入
        File file2 = new File(classpath + url.split("(/[\\w||-]+)[.]")[0]);
        if (!file2.exists()) {
            file2.mkdirs();
        }
        try {
            file.transferTo(new File(classpath + url.split("[.]")[0] + "." + file.getOriginalFilename().split("[.]")[1]));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
