package Java.SpringBoot3.exception;


import Java.SpringBoot3.domain.HttpResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j // for log.error(exception.getMessage());

//主要用來處理一般的 Exception
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage())
                        .developerMassage(exception.getMessage()) //這表示 developerMassage 欄位的值 完全等於 exception.getMessage() 的輸出。
                        .status(resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
                /*
    "statusCode": 500,
    "status": "INTERNAL_SERVER_ERROR",
    "reason": "No User found by email: KatieFagtn@gmail.com",
    "developerMassage": "No User found by email: KatieFagtn@gmail.com"
         */
    }

/*
what is the difference between ".status(HttpStatus.resolve(statusCode.value()))" and ".status(statusCode)"?

1. .status(statusCode)
    這個方法直接接受一個 整數型別的 HTTP 狀態碼（例如 200、400、500）。
    它不會檢查這個數字是否對應到 Spring 的 HttpStatus 列舉，只要是整數就能傳入。
    如果你傳入一個不存在的狀態碼（例如 999），它仍然會設定成 999，雖然這不是標準的 HTTP 狀態碼。
    👉 特點：直接設定數字，不驗證合法性。

2. .status(HttpStatus.resolve(statusCode.value()))
    HttpStatus.resolve(int statusCode) 會嘗試把整數轉換成 Spring 的 HttpStatus 列舉。
    如果該數字對應到一個合法的 HTTP 狀態碼（例如 200 → HttpStatus.OK），就會回傳對應的列舉。
    如果該數字沒有對應的列舉（例如 999），則回傳 null。
    當 .status(null) 被呼叫時，通常會拋出錯誤或導致回應無效。
    👉 特點：先檢查合法性，只有合法的狀態碼才會被接受。

簡單比喻 :
(1) .status(statusCode) 就像你直接在信封上寫一個號碼，不管這號碼是不是存在的郵遞區號，郵差都會照寫。
(2) .status(HttpStatus.resolve(statusCode)) 就像你先查郵遞區號表，確認這號碼是否存在，存在才寫上去；不存在就會失敗。
 */


/*
這段程式碼是 Spring MVC/Spring Boot 中的 全域例外處理方法，專門處理 MethodArgumentNotValidException（通常是因為 @Valid 驗證失敗）。

📌 流程說明
1. 攔截例外
     當 Controller 的參數驗證失敗（例如 @Valid 驗證欄位不符合規則），會拋出 MethodArgumentNotValidException。
     這個方法會被呼叫來處理該例外。
2. 記錄錯誤
     log.error(exception.getMessage()); → 把錯誤訊息寫到日誌。
3. 收集欄位錯誤
      exception.getBindingResult().getFieldErrors() → 取得所有欄位的錯誤。
      fieldErrors.stream().map(FieldError::getDefaultMessage) → 把每個欄位的錯誤訊息取出。
      Collectors.joining(", ") → 把多個錯誤訊息合併成一個字串，用逗號分隔。
4. 建立自訂錯誤回應
     使用 HttpResponse.builder() 建立一個統一格式的錯誤回應物件。
     包含：
        timeStamp → 錯誤發生時間。
        reason → 使用者可讀的錯誤原因（欄位錯誤訊息）。
        developerMassage → 開發者訊息（原始例外訊息）。
        status → HTTP 狀態（例如 BAD_REQUEST）。
        statusCode → 狀態碼數字（例如 400）。
5. 回傳 ResponseEntity
     new ResponseEntity<>(..., statusCode) → 把錯誤回應物件包裝成 ResponseEntity，並附上 HTTP 狀態碼。
 */
    @Override //LoginForm.java
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        /*
        "statusCode": 400,
        "status": "BAD_REQUEST",
        "reason": "Email cannot be empty, Password cannot be empty",
         */
        log.error(exception.getMessage());
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        /*
        String fieldMessage → 宣告字串變數。
        fieldErrors.stream() → 把 List 轉成 Stream。
        .map(FieldError::getDefaultMessage) → 方法參考 (Method Reference)，等同於 x -> x.getDefaultMessage()。
        .collect(Collectors.joining(", ")) → 把 Stream 的元素合併成一個字串，用逗號分隔。
         */
        String fieldMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString()) //記錄錯誤發生的時間。
                        .reason(fieldMessage) //顯示給使用者看的錯誤原因（所有欄位錯誤訊息）。
                        .developerMassage(exception.getMessage())//這表示 developerMassage 欄位的值 完全等於 exception.getMessage() 的輸出。
                        .status(resolve(statusCode.value()))
                        .statusCode(statusCode.value())
                        .build(), statusCode);
        /*
        回傳 ResponseEntity => return new ResponseEntity<>(..., statusCode);
                           => 把自訂的 HttpResponse 包裝成 ResponseEntity，並附上 HTTP 狀態碼。
         */
    }


    /*
    這個方法的作用是：
        當使用者送出資料違反資料庫唯一性約束（例如重複註冊相同 email），系統會捕捉到 SQLIntegrityConstraintViolationException。
        如果是「Duplicate entry」錯誤，就回傳 "Information already exists"，讓使用者明白資料已存在。
        同時回傳 400 Bad Request 狀態碼，並記錄詳細的例外訊息給開發者。
    */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<HttpResponse> sQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("Duplicate entry") ? "Information already exists" : exception.getMessage())
                        .developerMassage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
        /*
        {
  "timeStamp": "2026-01-24T16:15:00",
  "reason": "Information already exists",
  "developerMassage": "Duplicate entry 'test@example.com' for key 'users.email'",
  "status": "BAD_REQUEST",
  "statusCode": 400
}

         */
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponse> BadCredentialsException(BadCredentialsException exception) {
        /*
    "statusCode": 400,
    "status": "BAD_REQUEST",
    "reason": "Bad credentials, Incorrect email or password",
    "developerMassage": "Bad credentials"
         */
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage() + ", Incorrect email or password")
                        .developerMassage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<HttpResponse> apiException (ApiException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage())
                        .developerMassage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    //在 Spring MVC / Spring Boot 中，@ExceptionHandler 是用來指定 這個方法要處理哪一種例外。
    /*
    指定例外型別 :
        AccessDeniedException.class 是 Java 的 類別物件 (Class Object)，代表「這個方法要處理的例外型別」。
        如果不寫 .class，Spring 就不知道你要處理的是哪一個例外。

     Java 語法要求 :
        在 Java 中，AccessDeniedException 是一個類別名稱，而 AccessDeniedException.class 才是「類別物件」。
        @ExceptionHandler 的參數需要的是「類別物件陣列」，所以必須寫 .class。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponse> accessDeniedException(AccessDeniedException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("Access denied. You don't have access.")
                        .developerMassage(exception.getMessage())
                        .status(FORBIDDEN)
                        .statusCode(FORBIDDEN.value())
                        .build(), FORBIDDEN);
    }

    /* refresh token expired => throw the following exception => error 500 => internal server error
    {
    "timeStamp": "2026-01-...",
    "statusCode": 500,
    "status": "INTERNAL_SERVER_ERROR",
    "reason": "The Token has expired on 2023-01...",
    "developerMassage": "The Token has expired on 2023-01..."
}

{
    "timeStamp": "2026-01-...",
            "statusCode": 500,
            "status": "INTERNAL_SERVER_ERROR",
            "reason": "No User found by email: KatieFagtn@gmail.com",
            "developerMassage": "No User found by email: KatieFagtn@gmail.com"
}
    */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> exception(Exception exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())

        /* 這一段程式碼主要運用了 Java 的三元運算子 (ternary operator)，語法結構是：
            條件判斷 ? 條件為真時的值 : 條件為假時的值

         三元運算子可以巢狀使用：外層先判斷是否為 null，內層再判斷字串內容。
            (1)最外層判斷:
                    exception.getMessage() != null ? ... : "Some error occurred"
                        => 如果 exception.getMessage() 不是 null → 執行中間的判斷。
                        => 如果 exception.getMessage() 是 null → 回傳字串 "Some error occurred"。

            (2) 內層判斷（當訊息不為 null 時才會執行）:
                    exception.getMessage().contains("excepted 1, actual 0") ? "Record not found" : exception.getMessage()
                        => 如果訊息字串包含 "excepted 1, actual 0" → 回傳 "Record not found"。
                        => 否則 → 回傳原始的 exception.getMessage()。

            (3) 整體效果 :
                    如果訊息是 null → "Some error occurred"。
                    如果訊息包含 "excepted 1, actual 0" → "Record not found"。
                    如果訊息不為 null 且不包含該字串 → 原始訊息。
                         */
                        .reason(
                                exception.getMessage() != null ?
                                (
                                        exception.getMessage().contains("excepted 1, actual 0") ? "Record not found" : exception.getMessage()
                                )
                                : "Some error occurred"
                        )
                        .developerMassage(exception.getMessage())
                        .status(INTERNAL_SERVER_ERROR)
                        .statusCode(INTERNAL_SERVER_ERROR.value())
                        .build(), INTERNAL_SERVER_ERROR);
/*



為什麼是這個方法？
    (1) 狀態碼 500
        JSON 裡 statusCode: 500，對應到這個方法固定回傳的 INTERNAL_SERVER_ERROR。
    (2) reason 與 developerMassage 相同
        JSON 裡 reason 與 developerMassage 都是 "No User found by email: KatieFagtn@gmail.com"。
        這正是 exception.getMessage() 的值，符合這個方法的邏輯。
    (3) 條件判斷沒觸發
        方法裡有判斷 exception.getMessage().contains("excepted 1, actual 0") → 如果包含這段字串就改成 "Record not found"。
        但你的訊息是 "No User found by email: KatieFagtn@gmail.com"，不包含這段字串，所以直接回傳原始訊息。

1️⃣ exception.getMessage() :
在 Java 裡，Exception 類別有一個方法 getMessage()，它會回傳例外物件建立時的訊息字串。
=>  In UserRepositoryImpl,
=>  @Override
    //User → 回傳型別，這個方法會回傳一個 User 物件。
    //getUserByEmail(String email) → 方法名稱與參數，接收一個字串型別的 email。

    public User getUserByEmail(String email) {
        try {
            // jdbc.queryForObject(...) → 使用 Spring 的 JdbcTemplate 查詢資料庫。
            // new UserRowMapper() → 把查詢結果轉換成 User 物件。
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No User found by email: " + email);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

✅流程 :
UserRepositoryImpl.getUserByEmail(email)
   ↓
查不到使用者 → throw new ApiException("No User found by email: " + email)
   ↓
Controller 層沒有特別處理 → 交給 HandleException
   ↓
@ExceptionHandler(Exception.class) 捕捉到 ApiException
   ↓
exception.getMessage() → "No User found by email: KatieFagtn@gmail.com"
   ↓
包裝成 HttpResponse JSON → 回傳給前端

*/

    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<HttpResponse> emptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason(exception.getMessage().contains("excepted 1, actual 0") ? "Record not found" : exception.getMessage())
                        .developerMassage(exception.getMessage())
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> disabledException(DisabledException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .developerMassage(exception.getMessage())
                        // .reason(exception.getMessage() + ". Please check your email and verify your account.")
                        .reason("User account is currently disabled.")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponse> lockedException(LockedException exception) {
        log.error(exception.getMessage());
        return new ResponseEntity<>(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .developerMassage(exception.getMessage())
                        // .reason(exception.getMessage() + ", too many failed attempts.")
                        .reason("User account is currently locked.")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build(), BAD_REQUEST);
        /*
        In MySQLWorkbench, non-locked = 0 means that User account is currently locked.
        {
            "timeStamp": "2026-01-25T02:38:35.335958",
            "statusCode": 400,
            "status": "BAD_REQUEST",
            "reason": "User account is currently locked.",
            "developerMassage": "User account is locked"
        }
         */
    }
}





