package Java.SpringBoot3.query;







public class UserQuery {
    public static final String INSERT_USER_QUERY = "INSERT INTO Users (first_name, last_name, email, password) VALUES (:firstName, :lastName, :email, :password)";
    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM Users WHERE email = :email";
    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications (user_id, url) VALUES (:userId, :url)";
    public static final String SELECT_USER_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email = :email";
    public static final String DELETE_VERIFICATION_CODE_BY_USER_ID = "DELETE FROM TwoFactorVerifications WHERE user_id = :id";
    public static final String INSERT_VERIFICATION_CODE_QUERY = "INSERT INTO TwoFactorVerifications (user_id, code, expiration_date) VALUES (:userId, :code, :expirationDate)";
    public static final String SELECT_USER_BY_USER_CODE_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM TwoFactorVerifications WHERE code = :code)";
    public static final String DELETE_CODE = "DELETE FROM TwoFactorVerifications WHERE code = :code";
    public static final String SELECT_CODE_EXPIRATION_QUERY = "SELECT expiration_date < NOW() As is_expired FROM TwoFactorVerifications WHERE code = :code"; //The alias should be before the FROM keyword.
    public static final String DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY ="DELETE FROM ResetPasswordVerifications WHERE user_id = :userId";
    public static final String INSERT_PASSWORD_VERIFICATION_QUERY = "INSERT INTO ResetPasswordVerifications (user_id, url, expiration_date) VALUES (:userId, :url, :expirationDate)";
    public static final String SELECT_EXPIRATION_BY_URL = "SELECT expiration_date < NOW() AS is_expired FROM ResetPasswordVerifications WHERE url = :url";
    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)"; // FROM ResetPasswordVerifications table, find user_id by the url
    public static final String UPDATE_USER_PASSWORD_BY_URL_QUERY = "UPDATE Users SET password = :password WHERE id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)";
    public static final String DELETE_VERIFICATION_BY_URL_QUERY = "DELETE FROM ResetPasswordVerifications WHERE url = :url"; //delete that record because everything was successful (reset password successful). we just want to clean up everything.
    public static final String SELECT_USER_BY_ACCOUNT_URL_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM AccountVerifications WHERE url = :url)";
    public static final String UPDATE_USER_ENABLED_QUERY = "UPDATE Users SET enabled = :enabled WHERE id = :id";
    public static final String UPDATE_USER_DETAILS_QUERY = "UPDATE Users SET first_name = :firstName, last_name = :lastName, email = :email, phone = :phone, address = :address, title = :title, bio = :bio WHERE id = :id";
    public static final String SELECT_USER_BY_ID_QUERY = "SELECT * FROM Users WHERE id = :id";
    public static final String UPDATE_USER_PASSWORD_BY_ID_QUERY = "UPDATE Users SET password = :password WHERE id = :userId";
    public static final String UPDATE_USER_SETTINGS_QUERY = "UPDATE Users SET enabled = :enabled, non_locked = :notLocked WHERE id = :userId";
    public static final String TOGGLE_USER_MFA_QUERY = "UPDATE Users SET using_mfa = :isUsingMfa WHERE email = :email";
    public static final String UPDATE_USER_IMAGE_QUERY = "UPDATE Users SET image_url = :imageUrl WHERE id = :id";
    public static final String UPDATE_USER_PASSWORD_BY_USER_ID_QUERY = "UPDATE Users SET password = :password WHERE id = :id";
    /*
1. Users
        這是資料庫中的 資料表 (Table) 名稱。
        通常用來存放使用者資訊，例如 id, username, password, email 等欄位。
2. password
        這是 Users 資料表中的一個 欄位 (Column)。
        用來存放使用者的密碼（通常會經過加密或雜湊）。
3. :password
        這是 命名參數 (Named Parameter)。
        在執行 SQL 時，會由程式傳入一個值來替代它。
        例如：query.setParameter("password", hashedPassword);
4. id
        這是 Users 資料表中的主鍵欄位 (Primary Key)。
        每個使用者都有唯一的 id。
5. :userId
        這也是一個 命名參數。
        在執行 SQL 時，會由程式傳入一個使用者的 ID 值。
        例如：query.setParameter("userId", 123L);

✅ 總結 (繁體中文)
        Users → 資料表名稱。
        password → 欄位名稱。
        :password → 命名參數，程式傳入新密碼。
        id → 主鍵欄位。
        :userId → 命名參數，程式傳入使用者 ID。
*/
} //change to public -> need them to be accessible outside of this class
