package Java.SpringBoot3.domain;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;


@Data
@SuperBuilder
/*
@SuperBuilder
        → 提供建構器模式，支援繼承。
        生成一個 建構器模式 (Builder Pattern)。
        支援父類別與子類別的繼承情境，比 @Builder 更強大。
        讓你可以用鏈式呼叫來建立物件，例如：
        Role role = Role.builder()
                .id(1L)
                .name("ADMIN")
                .permission("READ_WRITE")
                .build();
 */
@NoArgsConstructor
/*
@NoArgsConstructor
        自動生成一個 無參數建構子。
        例如：Role role = new Role();
        常用於需要框架（像是 JPA、Jackson）建立物件時。
 */
@AllArgsConstructor
/*
@AllArgsConstructor
        自動生成一個 全參數建構子。
        例如：Role role = new Role(1L, "ADMIN", "READ_WRITE");
        方便快速建立完整物件。
 */
@JsonInclude(NON_DEFAULT) //在序列化成 JSON 時，只輸出非預設值的欄位，避免傳送多餘的空值。

/*
📖 角色在系統中的作用
User 與 Role 的關係 :
    User 代表使用者。
    Role 代表使用者的角色與權限。
    一般來說，使用者會被分配一個角色，用來控制他在系統中的操作權限。
舉例 :
    User: Alice → Role: ADMIN → 擁有管理權限。
    User: Bob → Role: USER → 只能瀏覽資料。


📖 總結 :
       Role.java 是一個簡單的 資料模型，用來描述系統中的角色與權限。
            它和 User.java 搭配使用，形成 使用者-角色-權限 的核心結構。
            在 API 回傳 JSON 時，會自動序列化成清晰的格式，並且只包含非預設值。
            Lombok 註解讓程式碼更簡潔，避免手動撰寫樣板程式。
 */
public class Role {
    private Long id; //→ 角色的唯一識別碼。
    private String name; //→ 角色名稱，例如 "ADMIN", "USER"。
    private String permission; // → 角色的權限描述，例如 "READ_WRITE", "READ_ONLY"。
}
