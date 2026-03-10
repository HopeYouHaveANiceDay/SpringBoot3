package Java.SpringBoot3.query;

public class RoleQuery {
    public static final String SELECT_ROLES_QUERY = "SELECT * FROM Roles ORDER BY id";
    public static final String INSERT_ROLE_TO_USER_QUERY = "INSERT INTO UserRoles (user_id, role_id) VALUES (:userId, :roleId)";
    public static final String SELECT_ROLE_BY_NAME_QUERY = "SELECT * FROM Roles WHERE name = :name";
    public static final String SELECT_ROLE_BY_ID_QUERY = "SELECT r.id, r.name, r.permission FROM Roles r JOIN UserRoles ur ON ur.role_id = r.id JOIN Users u ON u.id = ur.user_id WHERE u.id = :id";
    public static final String UPDATE_USER_ROLE_QUERY = "UPDATE UserRoles SET role_id = :roleId WHERE user_id = :userId";
}
/*
public static final String SELECT_ROLE_BY_ID_QUERY =
    "// 從 Roles 資料表查詢，並給它一個別名 r。選取角色的 id、name、permission 欄位。
     (1)SELECT r.id, r.name, r.permission
        FROM Roles r

     // 把 UserRoles 資料表和 Roles 表做 內連接 (JOIN)。
     // 條件：UserRoles 的 role_id 必須等於 Roles 的 id。
     // 這樣可以知道某個使用者被分配了哪些角色。
     (2)JOIN UserRoles ur ON ur.role_id = r.id

     // 再把 Users 資料表和 UserRoles 表做連接。
     // 條件：Users 的 id 必須等於 UserRoles 的 user_id。
     // 這樣可以把使用者和角色關聯起來。
     (3)JOIN Users u ON u.id = ur.user_id

     // 查詢條件：只選取指定使用者 ID 的角色。
     // :id 是一個 命名參數，在程式裡會被替換成實際的 userId 值。
     (4)WHERE u.id = :id";


📖 總結
這個 SQL 查詢的作用是：
   根據某個使用者的 id，查詢他所擁有的角色。
   查詢結果會包含角色的 id、名稱 (name) 和 權限 (permission)。
   透過 Roles、UserRoles、Users 三個表的連接，建立「使用者 → 角色」的關聯。
👉 簡單來說：這是一個「查詢某個使用者所屬角色」的 SQL 語句。
 */

/*
為什麼需要透過 Roles、UserRoles、Users 三個表的連接，以及它們是用來 insert 還是 query：

📖 為什麼要連接三個表
在典型的使用者與角色設計裡：
        Users 表：存放使用者基本資料（id、email、password…）。
        Roles 表：存放角色定義（id、name、permission…）。
        UserRoles 表：作為「關聯表 (join table)」，用來表示「哪個使用者擁有哪個角色」。
👉 因為一個使用者可能有多個角色，而一個角色也可能分配給多個使用者，所以需要 多對多 (Many-to-Many) 的關聯。
這就是為什麼要透過 三個表的 JOIN，才能查出「某個使用者擁有的角色」。

📖 是 insert 還是 query？
Roles 表：通常只在系統初始化或管理員新增角色時才會 insert，例如建立 ROLE_USER、ROLE_ADMIN。
Users 表：當有新使用者註冊時會 insert。
UserRoles 表：當要把某個角色分配給某個使用者時才會 insert，例如：
INSERT INTO UserRoles (user_id, role_id) VALUES (1, 2);
→ 表示 id=1 的使用者被分配了 id=2 的角色。

📖 總結
三表連接的目的：解決多對多關係，查出「使用者 → 角色」。
Roles：存角色定義，偶爾 insert。
Users：存使用者資料，註冊時 insert。
UserRoles：存使用者與角色的關聯，分配角色時 insert。
你看到的 SQL 是 查詢 (query)，不是 insert。
👉 簡單來說：Roles、Users 是資料表本身，UserRoles 是橋樑表。查詢時要 JOIN 三個表，新增使用者或角色時才會 insert。
 */