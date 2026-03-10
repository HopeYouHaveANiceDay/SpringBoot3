package Java.SpringBoot3.repository;
// 表示這個介面（interface）屬於 Java.SpringBoot3.repository 這個套件。
// 在 Java 中，package 用來組織程式碼，方便管理和避免類名衝突。

import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.dto.UserDTO;
import Java.SpringBoot3.form.UpdateForm;
import org.springframework.web.multipart.MultipartFile;
// 從 Java.SpringBoot3.domain 套件中引入 User 類。
// 這樣在這個檔案裡就可以直接使用 User 類，而不用寫完整路徑。

import java.util.Collection;


// 定義了一個 使用者資料存取介面：它屬於 repository 套件，用來存放資料存取邏輯。
public interface UserRepository<T extends User> {
// T extends User：這是一個 泛型 (Generic) 宣告，表示 T 必須是 User 類或其子類。
// 這樣設計的好處是：UserRepository 可以操作不同型別的使用者物件（例如 AdminUser、CustomerUser），只要它們繼承自 User。
    //使用泛型 T extends User，讓介面可以支援不同型別的使用者。

    /* Basic CRUD Operations */
    T create(T data); //建立使用者資料，傳入一個 User 或其子類物件，回傳建立後的物件。
    Collection<T> list(int page, int pageSize);//分頁查詢使用者清單，回傳一個集合。page 表示第幾頁，pageSize 表示每頁顯示多少筆資料。
    T get(Long id);//根據使用者的 id 查詢單一使用者。
    T update(T data);//更新使用者資料，傳入一個物件，回傳更新後的物件。
    Boolean delete(Long id);//根據 id 刪除使用者，回傳布林值表示是否成功。

    // More Complex Operations
    User getUserByEmail(String email);

    void sendVerificationCode(UserDTO user);

    User verifyCode(String email, String code);

    void resetPassword(String email);

    T verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    void renewPassword(Long userId, String password, String confirmPassword);

    T verifyAccountKey(String key);

    T updateUserDetails(UpdateForm user);

    void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword);

    void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked);

    User toggleMfa(String email);

    void updateImage(UserDTO user, MultipartFile image);
    /*
    (1) Repository 的角色 :
            UserRepository 是資料存取層 (DAO/Repository)，負責與資料庫互動。
            它通常提供方法來查詢、儲存、更新、刪除 User 實體。

    (2) 和 Service 層的關聯 :
            在 UserServiceImpl 裡，你會看到：
            @Override
            public UserDTO getUserByEmail(String email) {
                return UserDTOMapper.fromUser(userRepository.getUserByEmail(email));
            }`

            這裡就是呼叫 UserRepository.getUserByEmail(email)，把查到的 User 轉成 UserDTO 回傳給 Controller。
            如果 UserRepository 沒有這個方法，Service 層就無法查詢使用者。
 */
}

/*
📖 總結
這個 UserRepository 介面主要用來定義 使用者資料存取的基本操作 (CRUD)：
        create → 建立使用者
        list → 查詢使用者清單（支援分頁）
        get → 取得單一使用者
        update → 更新使用者
        delete → 刪除使用者
它的設計方式是透過 泛型 T extends User，讓介面可以靈活支援不同型別的使用者物件。
 */

/*
📖 為什麼需要 UserRepository :

分層設計 (Separation of Concerns)
        在 Spring Boot 或任何大型系統裡，通常會把「資料存取邏輯」和「業務邏輯」分開。
        UserRepository 就是專門負責「使用者資料存取」的介面，讓程式結構更清晰。
統一規範 (Contract)
        介面定義了一組方法（CRUD：建立、查詢、更新、刪除）。
        不管你用 MySQL、PostgreSQL、MongoDB 或記憶體中的集合來存資料，只要遵守這個介面，就能保證程式的其他部分可以正常呼叫。
可替換性 (Flexibility)
        如果未來要換資料庫或存取方式，只要提供新的 UserRepository 實作，不需要改動業務邏輯。
        例如：
        現在用 MySQL → UserRepositoryMySQLImpl
        未來改用 MongoDB → UserRepositoryMongoImpl
        介面讓這些替換變得容易。
泛型設計 (Generics)
        T extends User 讓介面支援不同型別的使用者（例如 AdminUser、CustomerUser）。
        這樣一個介面就能處理多種使用者類型，而不用為每個類型寫一套新的 CRUD。

📖 總結 :
        需要 UserRepository 的原因是：
        把資料存取邏輯抽象化，讓程式結構更乾淨。
        提供統一的操作規範，方便維護和擴展。
        支援不同的資料庫或存取方式，提升靈活性。
        使用泛型，讓介面能處理不同型別的使用者。
 */