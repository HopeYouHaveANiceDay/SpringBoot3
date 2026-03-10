package Java.SpringBoot3.rowmapper;

import Java.SpringBoot3.domain.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/*
📖 總結 :
    UserRowMapper 是一個 資料庫查詢結果轉換器。
    它會把 ResultSet 裡的每一列資料，轉換成一個 User 物件。
    常用於 JdbcTemplate.query(...)，讓查詢結果直接變成 Java 物件清單。
✅ 一句話總結：
    UserRowMapper 的作用就是把資料庫查詢結果的每一列，映射成 User 物件，方便在程式裡直接操作 Java 物件而不是原始 SQL 結果。
 */
// get the Object from the database
// 從 SQL 查詢 → ResultSet → RowMapper → User 物件
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getLong("id"))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .email(resultSet.getString("email"))
                .password(resultSet.getString("password"))
                .address(resultSet.getString("address"))
                .phone(resultSet.getString("phone"))
                .title(resultSet.getString("title"))
                .bio(resultSet.getString("bio"))
                .imageUrl(resultSet.getString("image_url"))
                .enabled(resultSet.getBoolean("enabled"))
                .isUsingMfa(resultSet.getBoolean("using_mfa"))
                .isNotLocked(resultSet.getBoolean("non_locked"))
                .createdAt(resultSet.getTimestamp("created_at").toLocalDateTime())
                .build();
    }
}
