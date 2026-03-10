package Java.SpringBoot3.handler;

import Java.SpringBoot3.domain.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


//訪問被拒絕處理程序
@Component //make @Bean by adding @Component//creating Bean and then pack up
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(now().toString())
                .reason("You don't have enough permission")
                .status(FORBIDDEN)
                .statusCode(FORBIDDEN.value())
                .build();
        response.setContentType(APPLICATION_JSON_VALUE);//設定回應格式為 JSON。
        response.setStatus(FORBIDDEN.value());//設定 HTTP 狀態碼為 403 Forbidden。

        //把後端產生的物件 (httpResponse) 轉換成 JSON 格式，並透過 HTTP 回應輸出串流送回給前端。
        OutputStream out = response.getOutputStream();
        /*
        OutputStream out = response.getOutputStream();
            => 從 HttpServletResponse 物件中取得輸出串流。
            => 這個串流代表伺服器要回傳給瀏覽器或前端的資料通道。
        Servlet 係由 Servlet 容器管理嘅 Java 物件，用嚟處理 HTTP 請求。
        */
        // 有用 getOutputStream()：你可以完全控制回應內容（例如 JSON 格式、錯誤訊息），並且在程式裡設定 403 Forbidden。
        // 沒有用 getOutputStream()：Spring Boot/Spring Security 會自動回傳 403 Forbidden 狀態，但通常是預設的 HTML 錯誤頁面。如果你要 JSON，就必須自己寫邏輯去「手動」設定回應。


        ObjectMapper mapper = new ObjectMapper();
        /*
        ObjectMapper mapper = new ObjectMapper();
            => 建立一個 Jackson 的 ObjectMapper 物件。
            => ObjectMapper 是 Java 常用的 JSON 處理工具，可以把 Java 物件轉換成 JSON，或把 JSON 轉換成 Java 物件。
         */
        mapper.writeValue(out, httpResponse);
        /*
        mapper.writeValue(out, httpResponse);
            => 使用 ObjectMapper 把 httpResponse 物件序列化成 JSON 格式。
            => 然後寫入到 out（也就是 HTTP 回應的輸出串流）。
            => 前端收到的就是一個 JSON 格式的錯誤訊息或回應。
         */

        out.flush();
        /*
        out.flush();
        => 把緩衝區的資料強制送出，確保 JSON 資料完整地回傳給前端。
         */
    }
}
