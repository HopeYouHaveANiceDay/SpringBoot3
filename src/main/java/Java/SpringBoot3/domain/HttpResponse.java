package Java.SpringBoot3.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;


//這段程式碼定義了一個 HttpResponse 類別，用來封裝 API 或 Web 應用程式回傳的標準格式。
@Data
@SuperBuilder
@JsonInclude(NON_DEFAULT)//例如：int statusCode = 0（預設值）就不會出現在 JSON 中。

public class HttpResponse { //HttpResponse response = HttpResponse.builder()
    // use "protected" here :
    // => we can access these from outside of this class and inside the same package
    protected String timeStamp; // .timeStamp("2026-03-08T19:30:00")
    protected int statusCode; // .statusCode(200)
    protected HttpStatus status; // .status(HttpStatus.OK)
    protected String reason; //.reason("OK")
    protected String message; //.message("查詢成功")
    protected String developerMassage; //.developerMassage("資料已正確回傳")
    protected Map<?, ?> data; //data：回傳的資料內容，通常是一個 Map，存放實際的業務資料。
    /*
        .data(Map.of(
        "user", Map.of("id", 1, "name", "Alice"),
        "orders", List.of(
            Map.of("orderId", 101, "amount", 250),
            Map.of("orderId", 102, "amount", 180)
        )
    ))
    .build();
     */
}
/*
data：回傳的資料內容，通常是一個 Map，存放實際的業務資料。give me an example
📖 範例程式碼建立回應 :
    HttpResponse response = HttpResponse.builder()
        .timeStamp("2026-03-08T19:30:00")
        .statusCode(200)
        .status(HttpStatus.OK)
        .reason("OK")
        .message("查詢成功")
        .developerMassage("資料已正確回傳")
        .data(Map.of(
            "user", Map.of("id", 1, "name", "Alice"),
            "orders", List.of(
                Map.of("orderId", 101, "amount", 250),
                Map.of("orderId", 102, "amount", 180)
            )
        ))
        .build();

🖥️ 輸出 JSON 範例 :
當這個物件序列化成 JSON 時，會長這樣：
{
  "timeStamp": "2026-03-08T19:30:00",
  "statusCode": 200,
  "status": "OK",
  "reason": "OK",
  "message": "查詢成功",
  "developerMassage": "資料已正確回傳",
  "data": {
    "user": {
      "id": 1,
      "name": "Alice"
    },
    "orders": [
      {
        "orderId": 101,
        "amount": 250
      },
      {
        "orderId": 102,
        "amount": 180
      }
    ]
  }
}

📌 說明
data 是一個 Map，可以存放不同型態的資料。
在這個例子裡，data 包含：
    user：使用者資訊（id、name）。
    orders：訂單清單（orderId、amount）。
這樣設計的好處是：API 回應格式統一，前端或其他服務只要解析 data 就能拿到需要的業務資料。
 */