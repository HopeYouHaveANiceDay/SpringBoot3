package Java.SpringBoot3.exception;
//表示這個類別屬於 Java.SpringBoot3.exception 套件，用來集中管理專案中的例外處理相關類別。

public class ApiException extends RuntimeException {
/*
public class ApiException extends RuntimeException { ... }
    定義了一個公開類別 ApiException，它繼承自 RuntimeException。
    RuntimeException 是 Java 的一種非檢查例外（unchecked exception），不需要在方法簽名裡用 throws 宣告。
    繼承它的好處是：在程式執行時可以拋出這個例外，並由 Spring Boot 的全域例外處理機制捕捉，回傳合適的錯誤訊息給 API 使用者。
 */
    public ApiException(String message) { super(message); }
    /*
    建構子 public ApiException(String message)
         接收一個字串 message 作為錯誤訊息。
         呼叫父類別 RuntimeException 的建構子 super(message)，把錯誤訊息傳遞給基底類別。
         這樣在拋出 ApiException 時，可以附帶自訂的錯誤訊息。
     */
}

/*
📖 使用場景 :
        當 API 執行過程中遇到錯誤（例如使用者輸入無效、找不到資源、驗證失敗），可以拋出 ApiException。
        Spring Boot 的 全域例外處理器 (Exception Handler) 可以捕捉這個例外，並回傳 JSON 格式的錯誤訊息給前端。

📖 總結 :
ApiException.java 的作用是：
        定義一個自訂的 API 例外類別。
        繼承 RuntimeException，方便在程式中拋出錯誤。
        讓 API 在發生錯誤時能提供清楚的訊息給使用者。
 */