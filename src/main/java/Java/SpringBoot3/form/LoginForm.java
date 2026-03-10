package Java.SpringBoot3.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm { //定義一個公開類別 LoginForm，用來封裝登入表單的資料。
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Invalid email. Please enter a valid email address.")
    private String email; //表示使用者在登入時必須輸入 email，不能留空。
    @NotEmpty(message = "Password cannot be empty")
    private String password;
}
