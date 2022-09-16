package spiritray.common.interceptor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * ClassName:HeaderJWTInterceptor
 * Package:spiritray.common.interceptor
 * Description:
 *
 * @Date:2022/4/24 9:22
 * @Author:灵@email
 */
@AllArgsConstructor
@Getter
public class HeaderJWTInterceptor implements ClientHttpRequestInterceptor {
    private final String headerName;
    private final String headerValue;


    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().set(headerName, headerValue);
        return execution.execute(request, body);
    }
}
