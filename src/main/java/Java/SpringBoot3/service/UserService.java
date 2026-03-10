package Java.SpringBoot3.service;

import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.dto.UserDTO;
import Java.SpringBoot3.form.UpdateForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/*
when the controller or the resource calls this service "UserService".
to get the user "(User user)", to create a new user "createUser(User user)".
then it's going to get the "UserDTO".
And not the user in "User.java" that has the password because the "UserDTO.java" doesn't have the password.
the user service is not returning the User but the UserDTO
 */


/*
📖 總結 :
 UserService 介面定義了四個方法：
    createUser → 建立新使用者並回傳 DTO。
    getUserByEmail → 透過 Email 查詢使用者並回傳 DTO。
    sendVerificationCode → 發送驗證碼給使用者。
    getUser → 透過 Email 查詢完整的使用者物件。
=> 這些方法通常會由一個 UserServiceImpl 類別去實作，並在系統中提供使用者相關的業務邏輯。
✅ 一句話總結：
UserService 是一個介面，規範了使用者建立、查詢、驗證碼發送等核心功能，讓系統能以一致的方式處理使用者相關操作。
 */
public interface UserService {

    UserDTO createUser(User user);
    /*
    1. UserDTO createUser(User user);
         功能：建立一個新的使用者。
         參數：User user → 前端或其他系統傳入的使用者物件。
         回傳：UserDTO → 建立完成後的使用者資料傳輸物件 (DTO)，通常只包含必要的資訊，避免直接暴露完整的 User 實體。
     */

    UserDTO getUserByEmail(String email);
    /*
    2. UserDTO getUserByEmail(String email);
         功能：透過 Email 查詢使用者。
         參數：String email → 使用者的電子郵件。
         回傳：UserDTO → 查詢到的使用者資料。
     */

    void sendVerificationCode(UserDTO user);
    /*
    3. void sendVerificationCode(UserDTO user);
         功能：發送驗證碼給使用者 (常用於登入或多因素驗證 MFA)。
         參數：UserDTO user → 需要接收驗證碼的使用者。
         回傳：void → 沒有回傳值，表示這個方法只執行動作。
     */


    UserDTO verifyCode(String email, String code);

    /*
    代表一個介面方法，用來：
        透過 Email 找到使用者。
        驗證使用者輸入的驗證碼是否正確。
        驗證成功後回傳一個 UserDTO 物件，通常包含使用者的基本資訊 (例如 email、名稱、角色)，但不會包含敏感資料 (例如密碼)。
     */
    void resetPassword(String email);

    UserDTO verifyPasswordKey(String key);

    void updatePassword(Long userId, String password, String confirmPassword);

    UserDTO verifyAccountKey(String key);

    /*You do not need to add @Valid again in your UserService.java method if you already have it in your UserResource.java controller method.
📌 Why?
(1) Validation happens at the controller boundary:
        When Spring MVC receives the request, it maps JSON → UpdateForm object.
        Because you annotated the parameter with @Valid, Spring will automatically validate UpdateForm against its constraints (@NotBlank, @Pattern, etc.) before calling your service method.
(2) Service layer doesn’t need @Valid:
        By the time updateUserDetails(UpdateForm user) is called, the user object is already validated.
        If validation fails, Spring throws a MethodArgumentNotValidException and the service method is never invoked.
     */
    UserDTO updateUserDetails(UpdateForm user);

    UserDTO getUserById(Long userId);

    void updatePassword(Long userId, String currentPassword, String newPassword, String confirmNewPassword);

    void updatedUserRole(Long userId, String roleName);

    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);

    UserDTO toggleMfa(String email);

    void updateImage(UserDTO user, MultipartFile image);
}

/*
功能：建立一個新使用者。
流程可能是：
        接收 User 物件。
        呼叫 Repository 把使用者存入資料庫。
        把存好的 User 轉換成 UserDTO。
        回傳 UserDTO 給 Controller，再由 Controller 回傳給前端。
 */