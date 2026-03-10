package Java.SpringBoot3.rowmapper;

import Java.SpringBoot3.domain.Role;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
/*
📖 總結 :
   這個 RoleRowMapper 類別的作用是：
    => 把資料庫查詢結果 (ResultSet) 的每一列，轉換成一個 Role 物件。
    => 讓 Spring JDBC 在執行 SQL 查詢時，可以自動把資料表中的 id、name、permission 欄位，對應到 Role 類別的屬性。
👉 簡單來說：RoleRowMapper 是一個「資料庫列 → Java 物件」的轉換器，專門用來把角色 (Role) 的資料表記錄轉換成程式裡的 Role 物件。
 */
public class RoleRowMapper implements RowMapper<Role> {
    @Override
    public Role mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return Role.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .permission(resultSet.getString("permission"))
                .build();
    }//@SuperBuilder in Role.java. so this is a pattern that we can use this Builder instead of creating a Constructor and then use the Setters and Getters
}//@SuperBuilder 用來自動生成 Builder 模式 的程式碼。與一般的 @Builder 不同，@SuperBuilder 特別適合 繼承結構 (Inheritance) 的情境。

/*
📖 範例 :
import lombok.experimental.SuperBuilder;

@SuperBuilder
class User {
    private String name;
    private String email;
}

@SuperBuilder
class Admin extends User {
    private String permissions;
}

public class Main {
    public static void main(String[] args) {
        Admin admin = Admin.builder()
                .name("Katie")
                .email("katie@example.com")
                .permissions("ALL")
                .build();

        System.out.println(admin);
    }
}

*** 執行結果 :
        Admin.builder() 可以同時設定父類別 (User) 的欄位和子類別 (Admin) 的欄位。
        最後生成一個完整的 Admin 物件。

 */

/*
📖 @SuperBuilder 建立物件的作用
當你使用：
java
Admin admin = Admin.builder()
        .name("Katie")
        .email("katie@example.com")
        .permissions("ALL")
        .build();
這段程式碼只是 在記憶體中建立一個 Java 物件，也就是一個 Admin 物件，裡面有 name、email、permissions 等屬性。
👉 它 不會自動插入資料庫。

📖 什麼時候會插入資料庫？
如果你想把這個 Admin 物件存到資料庫，需要透過 Repository / DAO / Service 去執行 SQL 或 ORM 操作。
在 Spring JDBC 裡，則需要用 jdbc.update(...) 或 jdbcTemplate.update(...) 搭配 SQL 語句，才能把物件的資料存進資料庫。


📖 總結 :
    Admin.builder().build() → 只是在程式裡建立一個物件（存在記憶體）。
    不會自動存到資料庫。
    要存入資料庫，必須透過 Repository / JDBC / JPA 等方式執行 SQL 或 ORM 操作。
👉 簡單來說：生成物件 ≠ 插入資料庫，它只是準備好一個物件，後續要不要存到資料庫，要看你是否呼叫了相關的資料存取方法。
 */