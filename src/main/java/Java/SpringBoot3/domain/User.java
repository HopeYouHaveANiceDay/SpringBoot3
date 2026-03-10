package Java.SpringBoot3.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_DEFAULT)

// we need to be able to map whatever we have in the database to this user in the Java code
// we're gonna build the controller so that we can make sure that all the data we're receiving
public class User {
    private Long id;
    //@NotEmpty(message = "First name cannot be empty")
    private String firstName;
    //@NotEmpty(message = "Last name cannot be empty")
    private String lastName;
    //@NotEmpty(message = "Email cannot be empty")
    //@Email(message = "Invalid email. Please enter a valid email address")
    private String email;
    //@NotEmpty(message = "Password cannot be empty")
    private String password;
    private String address;
    private String phone;
    private String title; //the title of the user whatever they do in our company
    private String bio;
    private String imageUrl;
    private boolean enabled; // if the account is active or inactive.
    private boolean isNotLocked;
    private boolean isUsingMfa;
    private LocalDateTime createdAt;
}

/*
為什麼需要 User.java，以及它和 UserRepository.java 的關係。

📖 為什麼需要 User.java
    User.java 是一個 領域模型 (Domain Model) 或 實體類 (Entity Class)，它的作用是：

映射資料庫表 :
        每個欄位（id, firstname, email, password 等）對應到資料庫中的一個欄位。這樣就能把資料庫的資料轉換成 Java 物件來操作。

封裝使用者資料 :
        在程式裡，所有跟使用者有關的資訊（名字、信箱、密碼、是否啟用等）都集中在這個類裡，方便管理。

驗證與序列化 :
        @NotEmpty, @Email 等註解用來做資料驗證，確保輸入正確。
        @JsonInclude(NON_DEFAULT) 控制 JSON 序列化時只輸出非預設值欄位。

簡化程式碼 :
        @Data 自動生成 getter/setter。
        @SuperBuilder 支援建構器模式。
        @NoArgsConstructor 和 @AllArgsConstructor 提供不同的建構子。



📖 User.java 與 UserRepository.java 的關係 :

User.java :
        定義「使用者」的資料結構。
        是一個 資料模型，代表一筆使用者資料。

UserRepository.java :
        定義「如何存取使用者資料」的介面。
        提供 CRUD 方法（建立、查詢、更新、刪除）。
        使用泛型 T extends User，表示它操作的物件必須是 User 或其子類。

👉 關係：
User.java 是 資料的形狀 (Data Model)。
        UserRepository.java 是 操作資料的方法 (Data Access Layer)。
        在程式裡，UserRepository 會接收或回傳 User 物件，讓 Service/Controller 可以方便地操作使用者資料。
 */