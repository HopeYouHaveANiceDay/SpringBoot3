package Java.SpringBoot3.rowmapper;

import Java.SpringBoot3.domain.UserEvent;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserEventRowMapper implements RowMapper<UserEvent> {

/*
In UserEvent.java,
    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(NON_DEFAULT)

    // all come from UserEvents and Events(type) in schema.sql
    public class UserEvent {
        private Long id;
        private String type; //→ 事件類型（例如 LOGIN、LOGOUT、UPDATE_PROFILE）。
        private String description;
        private String device; //device → 使用者操作的裝置（例如 "Chrome on MacOS"）。
        private String ipAddress;
        private LocalDateTime createdAt;
    }             */


    /*
✅ UserEventRowMapper 的作用 :
    這是 Spring JDBC 的一個工具，RowMapper<T> 用來把 SQL 查詢結果 (ResultSet) 轉換成 Java 物件。
    每一列查詢結果 (ResultSet) 都會被轉換成一個 UserEvent 物件。
    這裡用的就是 @SuperBuilder 提供的 builder() 方法，讓程式碼更簡潔。
    好處是：Repository 層查詢資料庫時，可以直接回傳 List<UserEvent>，而不是原始的 ResultSet。

     */
    @Override // if you go UserEvent.java, you can see all the @SuperBuilder

//在 Spring JDBC 裡，mapRow(ResultSet resultSet, int rowNum) 是 RowMapper 介面裡的一個方法。
// 它的作用是：把 SQL 查詢結果的一列 (row) 轉換成一個 Java 物件
//ResultSet rs → SQL 查詢的原始結果集，代表一列的資料。
    //int rowNum → 這一列在結果集裡的索引（第幾列），通常用來除錯或記錄，不常用。
    //回傳值 T → 你要轉換成的物件型別，例如 UserEvent。
    public UserEvent mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return UserEvent.builder()
                .id(resultSet.getLong("id"))
                .type(resultSet.getString("type"))
                .description(resultSet.getString("description"))
                .device(resultSet.getString("device"))
                .ipAddress(resultSet.getString("ip_address"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .build();

    }
}
/*
✅ 總結 :
原始的 ResultSet：SQL 查詢的低階結果，需要手動取值，難以直接使用。
@SuperBuilder 的 builder() 方法：Lombok 自動生成的 Builder，用來快速建立物件。
結合 RowMapper + builder()：把 ResultSet 轉換成 UserEvent 物件，讓資料庫查詢結果能自然銜接到你的業務邏輯。

整體流程
SQL 查詢 → 得到原始的 ResultSet。
RowMapper → 用 builder() 把每一列轉換成 UserEvent。
Service 層 → 使用 UserEvent 物件來執行業務邏輯。
Controller 層 → 把 UserEvent 物件轉成 JSON 回傳給前端。


原始的 ResultSet 範例 :
    如果你用 JDBC 查詢：
        ResultSet rs = statement.executeQuery("SELECT * FROM user_events");
        while (rs.next()) {
            Long id = rs.getLong("id");
            String type = rs.getString("type");
            String description = rs.getString("description");
            String device = rs.getString("device");
            String ip = rs.getString("ip_address");
            LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

            System.out.println(id + " " + type + " " + description + " " + device + " " + ip + " " + createdAt);
        }
    這就是 原始的 ResultSet：你必須一個欄位一個欄位手動取值，然後自己組合成物件或字串。
    缺點是程式碼冗長、可讀性差，而且容易出錯。


 */

/*
1. ResultSet resultSet
    代表 SQL 查詢的結果集。
    每次呼叫 mapRow，resultSet 就指向查詢結果中的一列 (row)。
    你可以用 resultSet.getString("columnName")、resultSet.getLong("columnName") 等方法取出這一列的欄位值。


2. int rowNum
    代表這一列在結果集裡的索引（第幾列）。
    例如：
        第一列 → rowNum = 0
        第二列 → rowNum = 1
        第三列 → rowNum = 2
    通常用來除錯或記錄，例如你想在 log 裡顯示「正在處理第幾列」。
    在大部分情況下，你不需要用到它，因為真正重要的是 ResultSet 裡的資料。

範例 :
假設資料庫有三筆事件：
id	type	        description
1	LOGIN_ATTEMPT	You tried to log in
2	LOGIN_SUCCESS	You logged in successfully
3	PROFILE_UPDATE	You updated your profile

查詢後，Spring 會依序呼叫 mapRow：
java
@Override
public UserEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
    System.out.println("正在處理第 " + rowNum + " 列");
    return UserEvent.builder()
            .id(rs.getLong("id"))
            .type(rs.getString("type"))
            .description(rs.getString("description"))
            .build();
}

輸出：
    正在處理第 0 列
    正在處理第 1 列
    正在處理第 2 列

✅ 總結
ResultSet → 查詢結果的一列資料。
rowNum → 這一列的索引，主要用來除錯或記錄，不常用。
mapRow 的核心工作就是：把 ResultSet 的一列轉換成一個 Java 物件（例如 UserEvent）。
 */