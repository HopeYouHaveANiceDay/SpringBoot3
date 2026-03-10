package Java.SpringBoot3.enumeration;

// come from schema.sql
public enum EventType {
    LOGIN_ATTEMPT("You tried to log in"),
    LOGIN_ATTEMPT_FAILURE("You tried to log in and you failed"),
    LOGIN_ATTEMPT_SUCCESS("You tried to log in and you succeeded"),
    PROFILE_UPDATE("You updated your profile information"),
    PROFILE_PICTURE_UPDATE("You updated your profile picture"),
    ROLE_UPDATE("You updated your role and permissions"),
    ACCOUNT_SETTINGS_UPDATE("You updated your account settings"),
    MFA_UPDATE("You updated your MFA settings"),
    PASSWORD_UPDATE("You updated your password");

/*
1. private final String description;
    這是一個欄位，用來存放事件的描述文字。
    final 表示它在建構時就必須被賦值，之後不能再改變。*/
    private final String description;

/*
2. EventType(String description)
    這是 enum 的建構子。
    當你定義常數時，例如：
    LOGIN_ATTEMPT("You tried to log in"),
    這裡的 "You tried to log in" 就會傳入建構子，存到 description 欄位。 */
    EventType(String description) {
        this.description = description;
    }

/*
3. public String getDescription()
    提供一個方法，讓外部程式可以取得這個 enum 常數的描述文字。
    例如：System.out.println(EventType.LOGIN_ATTEMPT.getDescription());
    會輸出：
    You tried to log in       */
    public String getDescription() {
        return this.description;
    }

/*
為什麼需要這樣設計？
    可讀性：enum 名稱通常簡短（如 LOGIN_ATTEMPT），但你可能需要更完整的描述給使用者或記錄到資料庫。
    避免硬編碼：不用在程式其他地方再寫 "You tried to log in"，直接透過 enum 就能取得。
    一致性：所有事件的描述都集中在 enum 裡，方便維護。

✅ 總結
    private final String description → 存放事件描述。
    建構子 → 在 enum 常數定義時初始化描述。
    getDescription() → 提供外部存取描述的方法。
    好處是讓 enum 不只是一個代號，還能附帶完整的文字說明，方便顯示、記錄和維護。
 */
}
