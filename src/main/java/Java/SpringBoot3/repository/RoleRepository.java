package Java.SpringBoot3.repository;

import Java.SpringBoot3.domain.Role;

import java.util.Collection;


/*
1. 泛型設計 T extends Role
這表示 RoleRepository 操作的物件必須是 Role 或其子類。
好處是：如果未來有不同型別的角色（例如 AdminRole, CustomerRole），都能透過這個介面來存取。 */
public interface RoleRepository<T extends Role> {
    /* Basic CRUD Operation*/
    T create(T data); // → 新增角色。
    Collection<T> List();//(int limit);//we can load specific number role //List(int limit) 是方法，参数 limit 用来限制返回的数量。
    T get(Long id);//→ 根據角色 ID 取得角色。
    T update(T data);//→ 更新角色資訊。
    Boolean delete(Long id);//→ 刪除角色。

    /* More Complex Operations */
    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId); //例如：查詢 ID 為 101 的使用者，可能回傳 Role("ROLE_ADMIN")。
    Role getRoleByUserEmail(String email);//例如：查詢 katie@example.com，可能回傳 Role("ROLE_USER")。
    void updateUserRole(Long userId, String roleName);//功能：更新指定使用者的角色，把舊角色換成新的角色。例如：把 ID 為 101 的使用者角色從 "ROLE_USER" 更新成 "ROLE_MANAGER"。回傳型別是 void，代表只執行更新，不回傳結果。
}
/*
📖 與 Role.java 的關係 :
        Role.java → 定義角色的資料模型（id, name, permission）。
        RoleRepository.java → 定義操作角色的方法（新增、查詢、更新、刪除、分配給使用者）。
        關係：RoleRepository 以 Role 作為操作對象，兩者一起構成「角色管理」的核心。
 */