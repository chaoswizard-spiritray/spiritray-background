package spiritray.common.pojo.BO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * ClassName:CommonInputStreamResource
 * Package:spiritray.common.pojo.BO
 * Description:
 *
 * @Date:2022/5/31 9:19
 * @Author:灵@email
 */
@Accessors(chain = true)
public class CommonInputStreamResource extends InputStreamResource {
    private long length;//文件大小
    private String originalFilename;//原文件名

    public CommonInputStreamResource(InputStream inputStream) {
        super(inputStream);
    }

    public CommonInputStreamResource(InputStream inputStream, long length, String originalFilename) {
        super(inputStream);
        this.length = length;
        this.originalFilename = originalFilename;
    }

    @Override
    public String getFilename() {
        return originalFilename;
    }

    @Override
    public long contentLength() throws IOException {
        return length == 0 ? 1 : 0;
    }
}
