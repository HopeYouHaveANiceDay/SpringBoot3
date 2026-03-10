// 指定這個類別屬於 Java.SpringBoot3.utils 套件。
//方便管理與引用，通常 utils 代表工具類別。
package Java.SpringBoot3.utils;

//匯入 UserDTO，這是一個資料傳輸物件 (Data Transfer Object)，用來封裝使用者的資料。
import Java.SpringBoot3.domain.UserPrincipal;
import Java.SpringBoot3.dto.UserDTO;

//匯入 Spring Security 的 Authentication 介面。
//Authentication 代表目前登入的使用者資訊，裡面包含 principal (使用者物件)、credentials (憑證)、authorities (權限)。
import org.springframework.security.core.Authentication;


/*
✅ 總結
UserUtils 是一個工具類別。
getAuthenticatedUser() 方法用來從 Spring Security 的 Authentication 物件中，取出目前登入的使用者 (UserDTO)。
好處是簡化程式碼，避免在 Controller 或 Service 裡重複寫 ((UserDTO) authentication.getPrincipal())。
 */

//定義一個工具類別 UserUtils，通常用來放置靜態方法，方便在專案中重複使用。
public class UserUtils {


    // 宣告一個 靜態方法，輸入參數是 Authentication。
    //作用是：從 Authentication 物件裡取出目前登入的使用者。
    public static UserDTO getAuthenticatedUser(Authentication authentication) {
        //authentication.getPrincipal() 代表「目前登入的使用者」。
        //這裡把它轉型成 UserDTO，方便在程式裡使用。
        return ((UserDTO) authentication.getPrincipal());
    }


    public static UserDTO getLoggedInUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
}
