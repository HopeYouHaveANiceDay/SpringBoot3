package Java.SpringBoot3.enumeration;
// enum (列舉)：一種特殊的類別，用來定義一組固定常量。
public enum RoleType {
    // 在這裡，RoleType 定義了系統中可能存在的角色類型.
    // 所有角色類型都集中在 RoleType 裡定義。
    // 如果要新增角色，只需要在 enum 裡加一個常量，不需要到處修改字串。
    ROLE_USER, ROLE_MANAGER, ROLE_ADMIN, ROLE_SYSADMIN

}
/*
解釋為什麼需要 RoleType 這個 enum，以及它在 roleRepository.addRoleToUser(user.getId(), ROLE_USER.name()); 中的作用。

 enum (列舉)：一種特殊的類別，用來定義一組固定常量。
        在這裡，RoleType 定義了系統中可能存在的角色類型：
        ROLE_USER → 一般使用者
        ROLE_MANAGER → 管理者
        ROLE_ADMIN → 系統管理員
        ROLE_SYSADMIN → 超級系統管理員


📖 為什麼需要它？
在 UserRepositoryImpl.java 中有這樣的呼叫：
roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

1. 避免硬編碼字串 :
        如果不用 enum，你可能會直接寫：
        roleRepository.addRoleToUser(user.getId(), "ROLE_USER");
        這樣容易出現錯字或不一致，例如 "ROLE_USRE"。
        使用 RoleType.ROLE_USER.name() 可以保證字串正確，因為它來自 enum 常量。

2. 集中管理角色 :
        所有角色類型都集中在 RoleType 裡定義。
        如果要新增角色，只需要在 enum 裡加一個常量，不需要到處修改字串。

3. 可讀性與維護性 :
        RoleType.ROLE_USER 比 "ROLE_USER" 更清晰，開發者一看就知道這是 enum 常量。
        在 IDE 裡也有自動提示，不容易寫錯。

4. .name() 的作用 :
        ROLE_USER 是 enum 常量。
        .name() 會回傳它的字串名稱 "ROLE_USER"。
        因為 addRoleToUser 方法需要字串參數，所以要用 .name() 把 enum 常量轉成字串。

📖 總結
    RoleType enum 定義了系統所有角色常量。
       在 UserRepositoryImpl 中使用 RoleType.ROLE_USER.name()，可以：
            避免硬編碼字串錯誤。
            集中管理角色。
            提升程式可讀性與維護性。
            .name() 把 enum 常量轉成字串，方便傳給資料庫或 repository 方法。
    👉 簡單來說：RoleType 是用來安全、統一地管理角色字串，避免錯字和混亂。


 */