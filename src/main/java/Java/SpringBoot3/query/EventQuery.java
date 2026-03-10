package Java.SpringBoot3.query;

public class EventQuery {
    // uev.id => local variable => "UserEvents table" in schema.sql
    // ev => local variable => "Events table" in schema.sql
    // joining two table => UserEvents & Events
    // older one => at the bottom
    // new one => at the top
    public static final String SELECT_EVENTS_BY_USER_ID_QUERY = "SELECT uev.id, uev.device, uev.ip_address, ev.type, ev.description, uev.created_at FROM Events ev JOIN UserEvents uev ON ev.id = uev.event_id JOIN Users u ON u.id = uev.user_id WHERE u.id = :id ORDER BY uev.created_at DESC LIMIT 10";
    public static final String INSERT_EVENT_BY_USER_EMAIL_QUERY = "INSERT INTO UserEvents (user_id, event_id, device, ip_address) VALUES ((SELECT id FROM Users WHERE email = :email), (SELECT id FROM Events WHERE type = :type), :device, :ipAddress)";
}

/*
這段程式碼定義了一個 SQL 插入語句常數，用來在資料庫裡新增一筆使用者事件：

public static final String INSERT_EVENT_BY_USER_EMAIL_QUERY =
    "INSERT INTO UserEvents (user_id, event_id, device, ip_address) VALUES (
        (SELECT id FROM Users WHERE email = :email),
        (SELECT id FROM Events WHERE type = :type),
        :device,
        :ipAddress
    )";

在 SQL 裡，VALUES (...) 是 INSERT 語句的一部分，用來指定要插入到資料表中的實際資料。
這裡的 VALUES (...) 裡面放了四個值，分別對應到 UserEvents 表的四個欄位：
1. (SELECT id FROM Users WHERE email = :email)
    → 子查詢，根據 email 找出使用者的 id。
2. (SELECT id FROM Events WHERE type = :type)
    → 子查詢，根據事件類型找出事件的 id。
3. :device
    → 命名參數，代表使用者的裝置。
4. :ipAddress
    → 命名參數，代表使用者的 IP 位址。


拆解說明
1. public static final String
        public → 任何地方都能存取。
        static → 屬於類別本身，不需要建立物件就能使用。
        final → 常數，不能被修改。
        String → 儲存 SQL 語句。
    👉 這樣寫的好處是：SQL 語句集中管理，避免硬編碼在程式裡。

2. SQL 語句內容
    INSERT INTO UserEvents (...) VALUES (...)
        → 新增一筆事件到 UserEvents 表。
    (SELECT id FROM Users WHERE email = :email)
        → 根據使用者的 email 找出對應的 user_id。
    (SELECT id FROM Events WHERE type = :type)
        → 根據事件的類型 (enum EventType) 找出對應的 event_id。
    :device, :ipAddress
        → 使用命名參數，代表使用者的裝置和 IP 位址。
3. 為什麼這樣設計
    避免多次查詢：不用先查 user_id 再查 event_id，直接在 INSERT 裡用子查詢完成。
    可讀性高：用命名參數 :email, :type, :device, :ipAddress，比 ? 佔位符更清楚。
    安全性：搭配 Spring 的 NamedParameterJdbcTemplate，可以防止 SQL Injection。
 */