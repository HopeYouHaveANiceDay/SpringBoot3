package Java.SpringBoot3.utils;

import Java.SpringBoot3.domain.HttpResponse;
import Java.SpringBoot3.exception.ApiException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.InvalidClaimException;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@Slf4j
public class ExceptionUtils {
/*
HttpServletRequest → 代表「請求物件」，用來讀取客戶端送來的資料。
HttpServletResponse → 代表「回應物件」，用來回傳資料給客戶端。
在處理錯誤時，你要回傳錯誤訊息給前端，所以必須用 HttpServletResponse。


差異解析
1. HttpServletResponse response
    來源：jakarta.servlet.http.HttpServletResponse
    層級：Servlet API，屬於 Java EE / Jakarta EE 的標準。
    用途：代表伺服器要回傳給瀏覽器或客戶端的「原始 HTTP 回應」。
    能做的事：
    設定 HTTP 狀態碼 (response.setStatus(200))
    設定 Header (response.setHeader("Content-Type", "application/json"))
    寫入 Body (response.getWriter().write("..."))
    👉 它是最低層的 API，直接操作 HTTP 回應。
2. HttpResponse httpResponse
    來源：你專案自訂的 Java.SpringBoot3.domain.HttpResponse 類別。
    層級：應用程式層，屬於你自己定義的資料模型。
    用途：用來封裝回應的結構，通常包含：
    timeStamp → 回應時間
    statusCode → 狀態碼
    status → 狀態文字（例如 OK, BAD_REQUEST）
    message → 訊息（例如 "Profile Retrieved"）
    data → 真正的業務資料（例如使用者資訊）
    👉 它是高層的封裝，方便前端解析 JSON。
 */


/*
這裡的 BAD_REQUEST 其實就是 HTTP 狀態碼 400 (Bad Request)，它的存在是為了讓伺服器在回應錯誤時，能夠清楚告訴客戶端「這個請求有問題」。
為什麼要用 BAD_REQUEST ?
(1) 標準化錯誤回應
    HTTP 協議裡定義了許多狀態碼：
    200 OK → 請求成功
    401 Unauthorized → 未授權
    403 Forbidden → 禁止存取
    400 Bad Request → 請求格式或內容錯誤
    用 BAD_REQUEST 可以讓客戶端（例如前端或 Postman）立即知道這不是伺服器內部錯誤，而是 請求本身有問題。
(2) 對應例外類型
    你的程式碼裡判斷的例外：ApiException, DisabledException, LockedException, InvalidClaimException, TokenExpiredException。
    這些錯誤通常代表 使用者的請求不符合要求（例如帳號被鎖定、Token 過期、驗證失敗）。
    所以用 400 Bad Request 是合理的，因為問題出在「請求」而不是伺服器。
(3) 方便前端處理
    前端收到 400 狀態碼時，可以根據錯誤訊息顯示提示（例如「帳號已停用」、「Token 已過期」）。
    如果用錯誤的狀態碼（例如 500 Internal Server Error），前端會誤以為是伺服器掛掉，而不是使用者操作錯誤。
 */





    /*
 這個 processError 方法的作用就是 統一處理系統在執行過程中拋出的例外 (Exception)，並將它轉換成標準化的 JSON 錯誤回應。
📖 方法流程解析
(1) 接收參數
    HttpServletRequest request → 當前的 HTTP 請求物件。
    HttpServletResponse response → 要回傳給客戶端的 HTTP 回應物件。
    Exception exception → 系統執行時拋出的例外。
(2) 判斷例外型別
    如果例外屬於以下型別：
        ApiException
        DisabledException（帳號停用）
        LockedException（帳號鎖定）
        BadCredentialsException（憑證錯誤，例如密碼錯誤）
        InvalidClaimException（JWT Claims 無效）
        TokenExpiredException（JWT Token 過期）
    系統會視為 客戶端錯誤 (400 Bad Request)。
(3) 建立錯誤回應 JSON
    使用 getHttpResponse(...) 建立一個 HttpResponse 物件，內容包含：
        timeStamp → 錯誤發生時間
        statusCode → 400
        status → BAD_REQUEST
        reason → 例外訊息 (exception.getMessage())
    例如：
            json
            {
              "timeStamp": "2026-01-23T14:57:54Z",
              "statusCode": 400,
              "status": "BAD_REQUEST",
              "reason": "The Token has expired on 2026-01-23T14:57:54Z."
            }`
(4) 處理其他例外
如果不是上述型別的例外，系統會視為 伺服器內部錯誤 (500 Internal Server Error)。
回應固定訊息：
json
{
  "timeStamp": "...",
  "statusCode": 500,
  "status": "INTERNAL_SERVER_ERROR",
  "reason": "An error occurred. Please try again."
}
寫入回應並記錄日誌
writeResponse(response, httpResponse) → 把 JSON 寫入 HTTP 回應。
log.error(exception.getMessage()) → 在伺服器端記錄錯誤訊息。

📖 總結
processError 的功能是：
把系統拋出的例外分類成「客戶端錯誤 (400)」或「伺服器錯誤 (500)」。
建立統一格式的 JSON 錯誤回應，回傳給前端。
同時在伺服器端記錄錯誤訊息，方便除錯。
✅ 一句話總結：
processError 是一個 全域錯誤處理方法，負責把例外轉換成標準化的 JSON 錯誤回應，讓前端收到一致的錯誤格式，而不是雜亂的系統堆疊訊息。
     */
    // 依個 processError method 唔會用係 public route 例如 refresh token !!!!
    // 因為 processError method 只用係 doFilter, not shouldNotFilter !!!
    public static void processError(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        // 如果例外屬於這些型別（例如 API 錯誤、帳號停用、帳號鎖定、憑證錯誤、JWT 聲明無效、Token 過期），系統會視為 客戶端錯誤。
        // 建立一個 HttpResponse，內容是例外訊息 (exception.getMessage())，狀態碼設為 400 Bad Request。
        /*
{
    "timeStamp": "2026-0.......",
    "statusCode": 400,
    "status": "BAD_REQUEST",
    "reason": "The Token has expired on 2026-01-23....."
}
         */
        if(exception instanceof ApiException || exception instanceof DisabledException || exception instanceof LockedException ||
                exception instanceof BadCredentialsException || exception instanceof InvalidClaimException) { //exception instanceof TokenExpiredException
            // catch (InvalidClaimException exception) {//如果 Token 的 Claims 無效（例如簽發者 withIssuer(GET_ARRAYS_LLC) 或受眾不符合），捕捉 InvalidClaimException。

            HttpResponse httpResponse = getHttpResponse(response, exception.getMessage(), BAD_REQUEST);
            writeResponse(response, httpResponse);

/* instanceof是Java中的二元运算符，左边是对象，右边是类；当对象是右边类或子类所创建对象时，返回true；否则，返回false。

PATCH http://localhost:8080/user/update
{
    "timeStamp": "2026-02-15T05:47:26.548794",
    "statusCode": 401,
    "status": "UNAUTHORIZED",
    "reason": "The Token has expired on 2026-02-14T21:27:19Z."
}
            */
        } else if (exception instanceof TokenExpiredException) {
            // 依個 processError method and its exception and send UNAUTHORIZED 唔會用係 public route 例如 refresh token !!!!
            // 因為 processError method 只用係 doFilter, not shouldNotFilter !!!
            // 所以直接去 controller => UserResource.java, 即係 @GetMapping("/refresh/token")
            // => UserDTO user = userService.getUserId(tokenProvider.getSubject(token, request));
            // => getSubject method (TokenProvider.java) => try to verify(token).getSubject() => fail because Token Expired => catch (TokenExpiredException exception) => throw exception;
            // => cut @RestControlAdvice which means sending 500 Error
            /*
@RestControllerAdvice
它是 @ControllerAdvice 的延伸版本，結合了 @ResponseBody。
作用：當 Controller 發生例外時，會自動攔截並執行你定義的處理方法，然後把結果以 JSON 格式回傳給前端。
如果沒有特別處理，Spring 會預設回傳 500 Internal Server Error。
             */
            HttpResponse httpResponse = getHttpResponse(response, exception.getMessage(), UNAUTHORIZED);
            writeResponse(response, httpResponse);

        // 如果不是上述型別的例外，則視為 伺服器內部錯誤。
        // 建立一個 HttpResponse，內容是固定提示 "An error occurred. Please try again."，狀態碼設為 500 Internal Server Error。
        } else {
            HttpResponse httpResponse = getHttpResponse(response, "An error occurred. Please try again.", INTERNAL_SERVER_ERROR);
            writeResponse(response, httpResponse);
        }
        log.error(exception.getMessage());
    }

/*
exception.printStackTrace(); 是 Java 中常見的除錯方式。
它會把例外（Exception）的完整堆疊追蹤資訊輸出到 標準錯誤輸出（stderr），通常就是你的 console 或 log。
這樣你可以清楚看到程式在哪一行拋出了例外、呼叫路徑是什麼，方便定位問題。

log.error(exception.getMessage()); 只會輸出錯誤訊息文字，例如 "Connection refused"。
exception.printStackTrace(); 則會輸出完整的堆疊追蹤，顯示錯誤發生的程式碼路徑。

📌 範例
假設程式在 mapper.writeValue(out, httpResponse); 這行拋出 NullPointerException，console 可能會顯示：
java.lang.NullPointerException (意思是，你（或你正在使用的程式碼）試圖使用一個物件變數，但這個變數的值是 null 。 你不能存取null 物件的方法/欄位等等，所以會拋出錯誤。)
    at com.example.MyClass.writeResponse(MyClass.java:25)
    at com.example.MyServlet.doGet(MyServlet.java:15)
    at javax.servlet.http.HttpServlet.service(HttpServlet.java:635)
    ...

這就是 printStackTrace() 的輸出效果。它會列出：
    例外類型 (NullPointerException)
    發生的程式碼行號 (MyClass.java:25)
    呼叫鏈（stack trace），一路往上追到 servlet 容器。

✅ 總結
exception.printStackTrace(); 是用來在除錯時快速查看錯誤的詳細資訊。
在正式環境中，通常會改用 logging framework（例如 Log4j、SLF4J）來記錄堆疊資訊，
而不是直接用 printStackTrace()，因為後者只會輸出到 console，難以集中管理。

============================================================

SLF4J 本身並不是只輸出到 console。它是一個 統一的日誌介面，真正的輸出位置取決於你後面綁定的 logging backend。
📌 SLF4J 的運作方式
    SLF4J：只是 API（介面），像一個抽象層。
    Backend（實作）：你可以選擇 Logback、Log4j2、java.util.logging 等。
    因此，日誌可以輸出到：
        Console（標準輸出）
        檔案（例如 app.log）
        遠端伺服器（透過 Socket、Syslog）
        雲端監控平台（ELK、Splunk、Datadog 等）

* */
    private static void writeResponse(HttpServletResponse response, HttpResponse httpResponse) {
        OutputStream out;
        try{
            out = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(out, httpResponse);
            out.flush();
        } catch (Exception exception) {
            log.error(exception.getMessage());
            exception.printStackTrace();
            /*
            exception.printStackTrace(); 的作用是 輸出完整的錯誤堆疊資訊，幫助開發者快速定位問題。
            雖然 log.error(exception.getMessage()) 已經記錄了錯誤訊息，但沒有行號與呼叫鏈路，除錯時資訊不足。
            因此兩者搭配使用：
            log.error(...) → 記錄簡單錯誤訊息。
            printStackTrace() → 顯示完整堆疊，方便追蹤。
             */
        }
    }

    private static HttpResponse getHttpResponse(HttpServletResponse response, String message, HttpStatus httpStatus) {
        HttpResponse httpResponse = HttpResponse.builder()
                .timeStamp(now().toString())
                .reason(message)
                .status(httpStatus)
                .statusCode(httpStatus.value())
                .build();
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        return httpResponse;
    }
}
