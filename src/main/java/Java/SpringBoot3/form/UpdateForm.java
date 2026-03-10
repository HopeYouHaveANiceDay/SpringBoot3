package Java.SpringBoot3.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateForm {
    //copy from user domain
    @NotNull(message = "ID cannot be null or empty.")
    private Long id;
    @NotEmpty(message = "First name cannot be empty")
    private String firstName;
    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email. Please enter a valid email address")

    private String email;
    @Pattern(regexp = "^\\d{11}$", message = "Invalid phone number")
    /*
這段程式碼是 Java Bean Validation (JSR 380) 的一個註解，用來驗證欄位值是否符合指定的正則表達式：
📖 拆解說明
(1) @Pattern
        來自 javax.validation.constraints.Pattern。
        用來檢查字串是否符合某個正則表達式 (regex)。
(2) regexp = "^\\d{11}$"
    這是正則表達式：
        ^ → 開頭
        \\d{11} → 11 個數字 (\\d 表示數字，{11} 表示重複 11 次)
        $ → 結尾
        整體意思：字串必須 完全由 11 位數字組成，不能多也不能少，不能有其他字元。

    ✅ 合法例子：
    "12345678901"
    "98765432100"
    ❌ 不合法例子：
    "1234567890" (只有 10 位)
    "123456789012" (12 位)
    "12345abc678" (包含字母)
    message = "Invalid phone number"
    當驗證失敗時，會回傳這個錯誤訊息。
    例如：如果使用者輸入 "12345"，驗證不通過，錯誤訊息就是 "Invalid phone number"。

📌 使用場景
通常會用在 DTO 或 Entity 的欄位上，例如：
public class UserDto {
    @Pattern(regexp = "^\\d{11}$", message = "Invalid phone number")
    private String phone;
}
當 Spring Boot 接收到表單或 JSON 請求時，會自動驗證 phone 欄位是否符合 11 位數字的規則。
     */


    //@NotEmpty(message = "Password cannot be empty")
    //private String password;
    private String phone;
    private String address;
    private String title; //the title of the user whatever they do in our company
    private String bio;
    //private String imageUrl;
    //private boolean enabled; // if the account is active or inactive.
    //private boolean isNotLocked;
    //private boolean isUsingMfa;
    //private LocalDateTime createdAt;
}
