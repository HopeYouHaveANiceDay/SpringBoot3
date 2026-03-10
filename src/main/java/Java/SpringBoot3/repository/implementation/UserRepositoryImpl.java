package Java.SpringBoot3.repository.implementation;

import Java.SpringBoot3.domain.Role;
import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.domain.UserPrincipal;
import Java.SpringBoot3.dto.UserDTO;
import Java.SpringBoot3.enumeration.VerificationType;
import Java.SpringBoot3.exception.ApiException;
import Java.SpringBoot3.form.UpdateForm;
import Java.SpringBoot3.repository.RoleRepository;
import Java.SpringBoot3.repository.UserRepository;
import Java.SpringBoot3.rowmapper.UserRowMapper;
import Java.SpringBoot3.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static Java.SpringBoot3.constant.Constants.DATE_FORMAT;
import static Java.SpringBoot3.enumeration.RoleType.ROLE_USER;
import static Java.SpringBoot3.enumeration.VerificationType.ACCOUNT;
import static Java.SpringBoot3.enumeration.VerificationType.PASSWORD;
import static Java.SpringBoot3.query.UserQuery.*;
// import static Java.SpringBoot3.utils.SmsUtils.sendSMS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Map.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.time.DateFormatUtils.*;
import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.*;


@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {

    private final NamedParameterJdbcTemplate jdbc; // we have this jdbc so we can make requests to the database and then query the database
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;

    /*
    1. private final EmailService emailService;
            宣告一個 EmailService 物件，用來處理寄送電子郵件的邏輯。
            final 表示這個物件在建立後不可再被重新指派。 */
    private final EmailService emailService;

    @Override
    public User create(User user) {
        // Check the email is unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0) throw new ApiException("Email already in use. Please use a different email and try again.");
        // Save new user
        try {//KeyHolder 是 Spring 提供的一個介面，用來存放 資料庫自動生成的鍵值（通常是主鍵 ID）。
            KeyHolder holder = new GeneratedKeyHolder(); //this is going to give us the ID of the user that just saved in a DB // use their ID to give the role
            SqlParameterSource parameters = getSqlParameterSource(user);
            jdbc.update(INSERT_USER_QUERY, parameters, holder);
            user.setId(requireNonNull(holder.getKey()).longValue());
            // Add role to user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            // Send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());//(1) ACCOUNT : 這是 VerificationType 列舉中的一個常量。在建立時，ACCOUNT("ACCOUNT") 把字串 "ACCOUNT" 存到欄位 type。 (2) .getType() : 這個方法會回傳 this.type.toLowerCase()。也就是把 "ACCOUNT" 轉成小寫字串 "account"。
            // Save URL in verification table : 把使用者 ID 和驗證 URL 存到 account_verification 資料表。
            jdbc.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, of("userId", user.getId(), "url", verificationUrl));
            sendEmail(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            // Send email to user with verification URL
            // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT); // ACCOUNT → 這是 VerificationType.ACCOUNT 的列舉常量，代表這封信是「帳號驗證」用途，而不是「密碼重設」。//just write ACCOUNT here because we use an if-statement to determine if this is account or if it's password.
            user.setEnabled(false);
            user.setNotLocked(true);
            System.out.println(verificationUrl);
            // Return the newly created user
            return user;
            // If any errors, throw exception with proper message
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

/*
/*
總結
這段程式碼的作用是：
在背景執行緒中非同步寄送驗證郵件，避免主程式被阻塞；若寄送失敗，則拋出 ApiException。

2. sendEmail(...) 方法
    這是一個私有方法，用來寄送驗證郵件。
    參數包括：使用者名字 (firstName)、電子郵件地址 (email)、驗證連結 (verificationUrl)、以及驗證類型 (verificationType)。
*/
private void sendEmail(String firstName, String email, String verificationUrl, VerificationType verificationType) {

/*
3. CompletableFuture.runAsync(...)
        使用 Java 的 CompletableFuture 來非同步執行任務。
        runAsync 會在背景執行緒中執行，不會阻塞主程式流程。
        好處是：寄送郵件可能需要時間，非同步處理可以避免主程式卡住。

4. new Runnable() { ... }
        建立一個匿名 Runnable 類別，裡面定義 run() 方法。
        在 run() 方法中，呼叫 emailService.sendVerificationEmail(...) 來真正寄送郵件。
 */
    //1. CompletableFuture<Void> future = CompletableFuture.runAsync(new Runnable() {
    //2. CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    //3.
    CompletableFuture.runAsync(() -> emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType));
        /*try {
            emailService.sendVerificationEmail(firstName, email, verificationUrl, verificationType);
        } catch (Exception exception) {
            throw new ApiException("Unable to send email");
        }
    });*/
}


    @Override
    public Collection<User> list(int page, int pageSize) {
        return List.of();
    }

    @Override
    public User get(Long id) {
        // return get(user.getId());
        // => means: “After updating the user in the database, fetch the updated record and return it.”
        try {
            //new UserRowMapper(): a RowMapper implementation that tells Spring how to convert the database row into a Java object.
            // => public class UserRowMapper implements RowMapper<User>  (UserRowMapperjava)
            return jdbc.queryForObject(SELECT_USER_BY_ID_QUERY, of("id", id), new UserRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No user found by id: " + id);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User update(User data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbc.queryForObject(COUNT_USER_EMAIL_QUERY, of("email", email), Integer.class);
    } // Integer.class : 指定查詢結果要轉換成 Integer 型別。

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if(user == null) { // if 「==」「等於」→ 表示資料庫沒有這個 email 的使用者。
            log.error("User not found in the database");
            throw new UsernameNotFoundException("User not found in the database");//拋出 UsernameNotFoundException → Spring Security 會認定登入失敗。
        } else {
            log.info("User found in the database: {}", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
    /*
      這段程式碼的作用是：
        根據 email 從資料庫查詢使用者。
        如果找不到 → 拋出 UsernameNotFoundException，登入失敗。
        如果找到 → 建立 UserPrincipal（實作 UserDetails），並附加角色資訊，交給 Spring Security 後續驗證。
    */
        }
    }

    //@Override 的作用是：告訴編譯器「這個方法是要覆寫 (override) 父類別或介面裡已經定義的方法」。
    //在 Java 中，當你 覆寫介面的方法 或 父類別的 public 方法 時，存取修飾子必須是 public，因為：
                /*
            使用 jdbc.queryForObject() 執行 SQL 查詢。
            SELECT_USER_BY_EMAIL_QUERY：一個 SQL 查詢字串，例如 SELECT * FROM users WHERE email = :email。
            of("email", email)：把查詢參數 email 傳入 SQL。
            new UserRowMapper()：用 UserRowMapper 把查詢結果 (ResultSet) 轉換成 User 物件。
            查詢成功後，會得到一個 User 物件並存入變數 user。
             */
    @Override
    public User getUserByEmail(String email) {
        try {
            User user = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No User found by email: " + email);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    //這段程式碼是一個 寄送驗證碼的服務方法
    public void sendVerificationCode(UserDTO user) {
        String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT); //the format is defined in here
        String verificationCode = randomAlphabetic(8).toUpperCase();
        try {
            //先刪除舊的驗證碼紀錄
            jdbc.update(DELETE_VERIFICATION_CODE_BY_USER_ID, of("id", user.getId()));
            // 插入新的驗證碼與到期日
            jdbc.update(INSERT_VERIFICATION_CODE_QUERY, of("userId", user.getId(), "code", verificationCode, "expirationDate", expirationDate));
            //sendSMS(user.getPhone(), "From: HopeYouHaveANiceDay \nVerification code\n" + verificationCode);

            // 寄送 Email，第四個參數是驗證類型
            //驗證碼的用途要看你傳入的第四個參數 VerificationType。
            //如果傳入 VerificationType.PASSWORD → 驗證碼用於密碼重設。
            //如果傳入 VerificationType.ACCOUNT → 驗證碼用於帳號。
            sendEmail(user.getFirstName(), user.getEmail(), verificationCode, ACCOUNT);
            log.info("Verification Code: {}", verificationCode);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    //這段程式碼是一個 Service 層的方法實作，名稱是 verifyCode，主要用來驗證使用者的 驗證碼 (code) 是否正確。
    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(code)) throw new ApiException("This code has expired. Please login again");
        try {
            /*
            1.查詢使用者 (依驗證碼)
            使用 jdbc.queryForObject 執行 SQL 查詢。
            SELECT_USER_BY_USER_CODE_QUERY → SQL 語句，透過驗證碼查詢使用者。
            of("code", code) → 把驗證碼參數傳入查詢。
            new UserRowMapper() → 把查詢結果轉換成 User 物件。
            結果：得到一個 User，代表輸入的驗證碼對應的使用者。
             */
            User userByCode = jdbc.queryForObject(SELECT_USER_BY_USER_CODE_QUERY, of("code", code), new UserRowMapper());

            /*
            2. 查詢使用者 (依 Email)
            同樣使用 jdbc.queryForObject 查詢。
            SELECT_USER_BY_EMAIL_QUERY → SQL 語句，透過 Email 查詢使用者。
            結果：得到一個 User，代表輸入的 Email 對應的使用者。
             */
            User userByEmail = jdbc.queryForObject(SELECT_USER_BY_EMAIL_QUERY, of("email", email), new UserRowMapper());

            /*
            3. 驗證 Email 是否一致
            equalsIgnoreCase → 忽略大小寫比較兩個 Email 是否相同。
            如果驗證碼查到的使用者與 Email 查到的使用者一致 → 驗證成功，回傳 userByCode。
            如果不一致 → 拋出 ApiException，訊息為「Code is invalid. Please try again.」。
             */
            if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                //make sure a verify code can be used one time only, and then delete
                jdbc.update(DELETE_CODE, of("code", code));
                return userByCode;
            } else {
                throw new ApiException("Code is invalid. Please try again.");
            }
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("Could not find record");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void resetPassword(String email) {
        if (getEmailCount(email.trim().toLowerCase()) <= 0) throw new ApiException("There is no account for this email address.");
        /*
        (1) incorrect email => http://localhost:8080/user/resetpassword/dbcvceudsnknbhf
            {
                "timeStamp": "2026-02-......",
                "statusCode": 400,
                "status": "BAD_REQUEST",
                "reason": "There is no account for this email address.",
                "developerMassage": "There is no account for this email address."
            }

        (2) correct email => http://localhost:8080/user/resetpassword/KatieFan@gmail.com
            {
                "timeStamp": "2026-02-02T23:23:52.783557",
                "statusCode": 200,
                "status": "OK",
                "message": "Email sent. Please check your email to reset your password."
            }
         */
        try {
                String expirationDate = format(addDays(new Date(), 1), DATE_FORMAT);
                /*
                這行程式碼的流程是：
                    取得現在時間 → new Date()
                    加一天 → addDays(...)
                    格式化成字串 → format(..., DATE_FORMAT)
                    存到 expirationDate 變數裡
                ✅ 一句話總結：
                    這行程式碼會生成一個字串，內容是「今天日期加一天」並依照 DATE_FORMAT 格式輸出，常用於設定 Token 或驗證碼的到期時間。
                 */
                User user = getUserByEmail(email);
                String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
                /*
                public enum VerificationType {
                    //we have two different types of URL either a new account or a password
                    ACCOUNT("ACCOUNT"),
                    PASSWORD("PASSWORD");
                 */
                jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", user.getId()));
                jdbc.update(INSERT_PASSWORD_VERIFICATION_QUERY, of("userId", user.getId(), "url", verificationUrl, "expirationDate", expirationDate));
                sendEmail(user.getFirstName(), email, verificationUrl, PASSWORD);
                log.info("Verification URL: {}", verificationUrl);
                /*
                the terminal output:
                There is no account for this email address.
                Verification URL: http://localhost:8080/user/verifypassword/98071c0d-593a-420d-9a46-5f66960657bb
                 */

                /*
                (1) "userId"
                    這是 Map 的 key。
                    在這裡代表 SQL 查詢裡的命名參數名稱。
                (2) user.getId()
                    這是 Map 的 value。
                    代表目前使用者的 ID。
                (3) 結果
                    會得到一個 Map：
                        { "userId" -> 4 }
                    如果 user.getId() 是 4，Map 就是 {"userId": 4}。

                📖 使用場景 :
                    在 Spring JDBC 或 NamedParameterJdbcTemplate 裡，常用這種 Map 來綁定 SQL 的命名參數，例如：
                    String sql = "INSERT INTO password_verification (user_id) VALUES (:userId)";
                    jdbc.update(sql, Map.of("userId", user.getId()));
                 這樣 :userId 就會被替換成 user.getId() 的值。

                 */
            } catch (Exception exception) {
                throw new ApiException("An error occurred. Please try again.");
            }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if(isLinkExpired(key, PASSWORD)) throw new ApiException("This link has expired. Please reset your password again.");
        try {                  // FROM ResetPasswordVerifications table, find user_id by the url
            User user = jdbc.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, of("url", getVerificationUrl(key, PASSWORD.getType())), new UserRowMapper());
            //only allow click that link once => jdbc.update("DELETE_USER_FROM_PASSWORD_VERIFICATION_QUERY", of("id", user.getId())); //Depends on use case / developer or business
            return user;
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Password don't match. Please try again.");
        /*
        not sending body, only POST url
        {http://localhost:8080/user/resetpassword/23a3ab8d-dac5-4c06-a537-ff8f8693a89e/789456/dchbcwncjch
        => 789456 (reset password)
        => dchbcwncjch (reset confirmPassword)
            "timeStamp": "2026-02...",
            "statusCode": 400,
            "status": "BAD_REQUEST",
            "reason": "Password don't match. Please try again.",
            "developerMassage": "Password don't match. Please try again."
        }
         */
        try {
            //UPDATE_USER_PASSWORD_BY_URL_QUERY → SQL 語句，應該是用 url 來找到對應的使用者並更新密碼。
            //of("password", ..., "url", ...) → 把參數綁定到 SQL。
            jdbc.update(UPDATE_USER_PASSWORD_BY_URL_QUERY, of("password", encoder.encode(password), "url", getVerificationUrl(key, PASSWORD.getType()))); // don't save the raw password
        /* find user id by that url
        (1) getVerificationUrl(...) → 方法，這是你自己定義的方法，用來生成驗證 URL。
            它通常會把 key 和 type 拼接到 URL 裡，例如：
            http://localhost:8080/user/verify/password/{key}

        (2) key → 隨機 Token，用來識別使用者。
              例如：98071c0d-593a-420d-9a46-5f66960657bb。

        (3) PASSWORD.getType() → 回傳 "password"，代表驗證類型。
                             PASSWORD 是你在 VerificationType 列舉裡定義的常量：(VerificationType.java)
                             PASSWORD("PASSWORD");
                             呼叫 getType() 會回傳 "password"（因為你在方法裡轉成小寫）。
                             這代表驗證的類型是「密碼」。


        合起來就是生成一個「密碼驗證連結」。
         */
            jdbc.update(DELETE_VERIFICATION_BY_URL_QUERY, of("url", getVerificationUrl(key, PASSWORD.getType())));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }


    @Override
    public void renewPassword(Long userId, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Password don't match. Please try again.");
        try {
            //UPDATE_USER_PASSWORD_BY_URL_QUERY → SQL 語句，應該是用 url 來找到對應的使用者並更新密碼。
            //of("password", ..., "url", ...) → 把參數綁定到 SQL。
            jdbc.update(UPDATE_USER_PASSWORD_BY_USER_ID_QUERY, of("id", userId, "password", encoder.encode(password)));

            //delete it after verifying the account
            // jdbc.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, of("userId", userId));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }


    @Override
    public User verifyAccountKey(String key) {
        try {
            // 這行程式碼的作用是：從資料庫查詢一筆使用者資料，並且把結果轉換成 User 物件。最後把這個 User 存到變數 user
            User user = jdbc.queryForObject(SELECT_USER_BY_ACCOUNT_URL_QUERY, of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());
            jdbc.update(UPDATE_USER_ENABLED_QUERY, of("enabled", true, "id", user.getId()));
            //Delete after updating - depends on your requirements
            return user;
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("This link is not valid.");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    /*
    📌 Step-by-step explanation
1. jdbc.update(...)
    Executes the SQL update query using the values from UpdateForm.
    This changes the user’s details in the database.
2. return get(user.getId());
    After updating, the method calls another function get(...) to fetch the user record from the database by ID.
    This ensures the method returns the latest, fully updated User object (not just the input form).
3. Why not just return user?
    UpdateForm is usually a DTO (data transfer object) with only the fields being updated (e.g., name, phone).
    The actual User entity in the database may contain more fields (e.g., createdDate, role, status).
    By calling get(user.getId()), you retrieve the complete and current User entity after the update.
4. Error handling
    If something goes wrong during the update, the catch block logs the error and throws a custom ApiException.
     */
    @Override
    public User updateUserDetails(UpdateForm user) {
        try {
            jdbc.update(UPDATE_USER_DETAILS_QUERY, getUserDetailsSqlParameterSource(user));
// UpdateForm is usually a DTO (data transfer object) with only the fields being updated (e.g., name, phone).
// fetch the user record from the database by ID.
            return get(user.getId());
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No user found by id: " + user.getId());
        }catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        }
    }

    @Override
    public void updatePassword(Long id, String currentPassword, String newPassword, String confirmNewPassword) {
        if(!newPassword.equals(confirmNewPassword)) {throw new ApiException("Passwords don't match.Please try again."); }
        User user = get(id); //bet user by id
        if(encoder.matches(currentPassword, user.getPassword())) {
            try {
                jdbc.update(UPDATE_USER_PASSWORD_BY_ID_QUERY, of("userId", id, "password", encoder.encode(newPassword)));
            } catch (Exception exception) {
                throw new ApiException("An error occurred. Please try again.");
            }
        } else {
            throw new ApiException("Incorrect current password. Please try again.");
        }
    }

    @Override
    public void updateAccountSettings(Long userId, Boolean enabled, Boolean notLocked) {
        try {
            // no return due to void
            jdbc.update(UPDATE_USER_SETTINGS_QUERY, of("userId", userId, "enabled", enabled, "notLocked", notLocked));
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred Please try again.");
        }
    }

    //這個 toggleMfa 方法的作用是讓使用者開關 多因素驗證 (MFA)
    @Override
    public User toggleMfa(String email) {
        // 1. 先透過 email 找到使用者
        User user = getUserByEmail(email);
        // 2. 檢查使用者是否有電話號碼
        if(isBlank(user.getPhone())) {throw new ApiException("You need a phone number to change Multi-Factor Authentication"); }
        // 3. 切換 MFA 狀態 (true → false, false → true)
        user.setUsingMfa(!user.isUsingMfa());
        try {
            // 4. 更新資料庫，執行 SQL
            jdbc.update(TOGGLE_USER_MFA_QUERY, of("email", email, "isUsingMfa", user.isUsingMfa()));
            // 5. 回傳更新後的使用者物件
            return user;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("Unable to update Multi-Factor Authentication");
        }
    }


    //這段程式碼的作用是讓使用者更新自己的 個人圖片，並且把圖片存到伺服器檔案系統，同時更新資料庫裡的圖片 URL。
    @Override
    public void updateImage(UserDTO user, MultipartFile image) {

/*  1. 產生圖片 URL :
          setUserImageUrl(user.getEmail())：根據使用者 email 建立圖片 URL，例如：
             http://localhost:8080/user/image/john@example.com.png
           這個 URL 會用來讓前端存取圖片。 */
        String userImageUrl = setUserImageUrl(user.getEmail());

/*  2. 更新使用者物件 : user.setImageUrl(userImageUrl)：把新圖片 URL 存到使用者物件。  */
        user.setImageUrl(userImageUrl);

/*  3. 存檔 : saveImage(user.getEmail(), image)：把上傳的圖片存到伺服器檔案系統。*/
        saveImage(user.getEmail(), image);

/* 4. 更新資料庫: jdbc.update(...)：執行 SQL 更新，把圖片 URL 存到資料庫。   */
        jdbc.update(UPDATE_USER_IMAGE_QUERY, of("imageUrl", userImageUrl, "id", user.getId()));
    }

/* 設定圖片 URL :
        使用 ServletUriComponentsBuilder.fromCurrentContextPath() 取得目前伺服器的 base URL。
        拼接成 /user/image/{email}.png。
        這樣前端就能透過 URL 直接存取圖片。  */
    private String setUserImageUrl(String email) {
        return fromCurrentContextPath().path("/user/image/" + email + ".png").toUriString();
    }

    //we try to save the image on the server
    //這個 saveImage 方法的作用是：把使用者上傳的圖片存到伺服器檔案系統，並且確保目錄存在，舊檔會被覆蓋。
    private void saveImage(String email, MultipartFile image) {

/*  1. 設定存檔路徑 :
        使用 System.getProperty("user.home") 取得使用者主目錄。
        拼接成 ~/Downloads/images/。
        例如在 Windows：C:/Users/你的帳號/Downloads/images/。    */
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/images/").toAbsolutePath().normalize();

/*  2. 建立資料夾 :
        如果 images 資料夾不存在，建立它。
        若建立失敗，拋出 ApiException。     */
        if (!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (Exception exception) {
                log.error(exception.getMessage());
                throw new ApiException("Unable to create directories to save image");
            }
            log.info("Created directories: {}", fileStorageLocation);
        }
        try {
            //get rid of the old one, replace it
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(email + ".png"), REPLACE_EXISTING);
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new ApiException(exception.getMessage());
        }
        log.info("File saved in: {} folder", fileStorageLocation);
    }

    private Boolean isLinkExpired(String key, VerificationType password) {
        try {
            return jdbc.queryForObject(SELECT_EXPIRATION_BY_URL, of("url", getVerificationUrl(key, password.getType())), Boolean.class);
        } catch (EmptyResultDataAccessException exception) {
            log.error(exception.getMessage());
            throw new ApiException("This link is not valid. Please reset your password again.");
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private Boolean isVerificationCodeExpired(String code) {
        try {// We use Boolean to check the code. If expiration_date is less than Now(), return true. Which means the code is expired and throw exception.
            return jdbc.queryForObject(SELECT_CODE_EXPIRATION_QUERY, of("code", code), Boolean.class);
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("This code is not valid. Please login again.");
        } catch (Exception exception) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    private SqlParameterSource getSqlParameterSource(User user) {
        return new MapSqlParameterSource() // serve a new user in the database
                .addValue("firstName", user.getFirstName()) // (1) 不是從資料庫取值，而是 把程式裡的 User 物件值（例如 "Katie"）傳給 SQL。(UserQuery.java)
                .addValue("lastName", user.getLastName())   // (2) 這樣 SQL 的 :firstName 參數就會被替換成 "Katie"，最後把 "Katie" 存到資料庫的 firstName 欄位。
                .addValue("email", user.getEmail())         // (3) 簡單來說：這是把 User 物件的資料安全地插入資料庫的方式。
                .addValue("password", encoder.encode(user.getPassword()));  //     => 即使使用者輸入惡意字串，框架也會把它當成「純文字」，而不是 SQL 指令。
    } // cannot store the raw password in the database

    private SqlParameterSource getUserDetailsSqlParameterSource(UpdateForm user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone", user.getPhone())
                .addValue("address", user.getAddress())
                .addValue("title", user.getTitle())
                .addValue("bio", user.getBio());
    }

    // 這個方法的作用是：根據目前應用程式的 Context Path，動態生成一個驗證用的 URL。
    private String getVerificationUrl(String key, String type) {
        // type → 驗證的類型（例如 email、phone）。
        // key → 驗證用的唯一識別碼 random UUID（例如 Token 或隨機字串）。
        // http://localhost:8080/user/verify/email/abc123
        return fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
        //http://localhost:8080/user/verify/password/98071c0d-593a-420d-9a46-5f66960657bb
/*
(1) ServletUriComponentsBuilder.fromCurrentContextPath()
    建立一個 URI Builder，基於目前應用程式的 Context Path。
    例如，如果你的應用程式部署在 http://localhost:8080/myapp，這裡就會以 http://localhost:8080/myapp 為基底。

(2) .path("/user/verify" + type + "/" + key)
    在基底 URL 後面加上路徑。
    type → 驗證的類型（例如 email、phone）。
    key → 驗證用的唯一識別碼（例如 Token 或隨機字串）。
    組合後的路徑可能是：
    /user/verify/email/abc123
    /user/verify/phone/xyz789

(3) .toUriString()
    把整個 URI Builder 轉換成字串。
    最後回傳完整的 URL。
*/
    }

}

/*
為什麼在 Spring JDBC 裡需要 KeyHolder。
📖 背景 :
        當我們用 JdbcTemplate 或 NamedParameterJdbcTemplate 執行 INSERT 語句時，
        如果資料表的主鍵是自動生成的（例如 MySQL 的 AUTO_INCREMENT 或 PostgreSQL 的 SERIAL），
        通常我們希望在插入新資料後，能夠馬上取得這個新生成的主鍵 ID。

📖 為什麼需要 KeyHolder :
(1) 取得自動生成的主鍵 :
        當你新增一筆使用者資料時，資料庫會自動生成一個唯一的 ID。
        KeyHolder 就是用來接收並保存這個 ID。
        例如：新增 Katie 到 users 表後，系統會回傳 id = 101，這個值就存放在 KeyHolder 裡。

(2) 方便後續操作 :
        新增資料後，通常需要馬上用這個 ID 做其他事：
            把使用者的角色存到 user_roles 表。
            建立驗證 URL 並綁定到該使用者。
        如果沒有 KeyHolder，你就不知道新插入的那筆資料的 ID。

(3) 避免額外查詢
        沒有 KeyHolder 的話，你可能需要再執行一次 SELECT 查詢來找剛剛插入的資料。
        這樣效率低，而且可能有競爭條件（多個使用者同時註冊時，查詢結果可能不準確）。
        使用 KeyHolder 可以直接拿到資料庫生成的主鍵，避免這些問題。
*/