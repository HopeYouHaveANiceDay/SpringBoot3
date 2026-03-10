CREATE SCHEMA IF NOT EXISTS SpringDB;

-- 設定字元編碼
SET NAMES utf8mb4;

-- 設定時區（選擇一種方式）
SET time_zone = 'America/New_York'; -- 紐約時間

USE SpringDB;


/*
 How to fix :
    When dropping tables: drop child tables first, then parent tables.
    When creating tables: create parent tables first, then child tables.

 建立順序：父表 → 子表
 刪除順序：子表 → 父表
 */

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS TwoFactorVerifications;
DROP TABLE IF EXISTS ResetPasswordVerifications;
DROP TABLE IF EXISTS AccountVerifications;
DROP TABLE IF EXISTS UserEvents;
DROP TABLE IF EXISTS UserRoles;
DROP TABLE IF EXISTS Events;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS Users;

SET FOREIGN_KEY_CHECKS = 1;



-- DROP TABLE IF EXISTS Users;

CREATE TABLE Users
(
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name   VARCHAR(50) NOT NULL,
    last_name    VARCHAR(50) NOT NULL,
    email        VARCHAR(100) NOT NULL,
    password     VARCHAR(255) DEFAULT NULL, -- 預設值是 NULL，也就是說如果新增使用者時沒有提供密碼，這個欄位可以是空的。 we can have a user without the password
    address      VARCHAR(255) DEFAULT NULL,
    phone        VARCHAR(30) DEFAULT NULL,
    title        VARCHAR(50) DEFAULT NULL,  -- title：職稱
    bio          VARCHAR(255) DEFAULT NULL, -- bio：簡介
    enabled      BOOLEAN DEFAULT FALSE, -- enabled：表示帳號是否啟用，預設為 FALSE（未啟用）。
    non_locked   BOOLEAN DEFAULT TRUE, -- non_locked：表示帳號是否未被鎖定，預設為 TRUE（未鎖定）。
    using_mfa    BOOLEAN DEFAULT FALSE, --  this Boolean is going to tell us if the user is using MFA or not
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP, -- 預設為當前時間。
    image_url    VARCHAR(255) DEFAULT 'https://cdn-icons-png.flaticon.com/512/149/149071.png',
    CONSTRAINT UQ_Users_Email UNIQUE (email)
);

-- DROP TABLE IF EXISTS Roles;

CREATE TABLE Roles
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    permission VARCHAR(255) NOT NULL,
    CONSTRAINT UQ_Roles_Name UNIQUE (name) -- 每個role的名稱必須是唯一的，不能有兩個roles同名。
);




-- DROP TABLE IF EXISTS Events;

CREATE TABLE Events
(

    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,

    -- CHECK(...)  ->
    -- 是一個 檢查約束（Check Constraint），用來限制欄位可接受的值，確保資料符合指定的條件。
    -- USE SpringDB;
    -- INSERT INTO Events (type, description) VALUES ('ACCOUNT_SETTINGS_UPDATE', 'You updated your account settings');

-- 你在執行這些 INSERT INTO Events ... 語句時遇到 Error Code: 3819，這個錯誤在 MySQL 裡的意思是：CHECK constraint failed（檢查約束失敗）

-- (1) 檢查表格定義 :
--     SHOW CREATE TABLE Events; 確認 CHECK 約束裡的字串是否正確包含 'ACCOUNT_SETTINGS_UPDATE' 和 'MFA_UPDATE'。

-- (2) 刪掉舊的約束：
--     ALTER TABLE Events DROP CHECK events_chk_1; 刪掉舊的約束後再新增正確的約束

-- (3) 刪掉舊約束後重新建立新的：
--     ALTER TABLE Events
--     ADD CONSTRAINT chk_event_type CHECK (type IN ( ... ));

    -- type IN (...)  -> 表示 type 欄位的值只能在指定的集合裡，例如：'LOGIN_ATTEMPT'....
    type        VARCHAR(50) NOT NULL CHECK(type IN ('LOGIN_ATTEMPT', 'LOGIN_ATTEMPT_FAILURE', 'LOGIN_ATTEMPT_SUCCESS', 'PROFILE_UPDATE', 'PROFILE_PICTURE_UPDATE', 'ROLE_UPDATE', 'ACCOUNT_SETTINGS_UPDATE', 'MFA_UPDATE', 'PASSWORD_UPDATE')),
    description VARCHAR(255) NOT NULL,
    CONSTRAINT UQ_Events_Type UNIQUE (type)
);


-- DROP TABLE IF EXISTS UserRoles; -- establish the relationship the users and their roles

CREATE TABLE UserRoles
(
    id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    -- ON DELETE CASCADE: if a user is deleted, their related UserRoles record will also be deleted.
    FOREIGN KEY (role_id) REFERENCES Roles (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    -- ON DELETE RESTRICT: we cannot delete a role if it is still assigned to a user.
    CONSTRAINT UQ_UserRoles_User_Id UNIQUE (user_id)
);



-- DROP TABLE IF EXISTS UserEvents;

CREATE TABLE UserEvents
(
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT UNSIGNED NOT NULL,
    event_id   BIGINT UNSIGNED NOT NULL,
    device     VARCHAR(100) DEFAULT NULL, -- device -> Chrome or a Linux system
    ip_address VARCHAR(100) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (event_id) REFERENCES Events (id) ON DELETE RESTRICT ON UPDATE CASCADE
); -- we remove "CONSTRAINT... UNIQUE (...)" in that table because we can have multiple different events for the same user so we keep multiple records of the same user in that table





-- DROP TABLE IF EXISTS AccountVerifications;

CREATE TABLE AccountVerifications
(
    id      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNSIGNED NOT NULL,
    url     VARCHAR(255) NOT NULL,
    -- date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    -- ON DELETE CASCADE：如果某個使用者被刪除，相關的驗證記錄也會自動刪除。
    -- ON UPDATE CASCADE：如果 Users 表的 id 更新，這裡的 user_id 也會跟著更新。
    CONSTRAINT UQ_AccountVerifications_User_Id UNIQUE (user_id), -- 確保每個使用者在 AccountVerifications 表裡只能有一筆驗證記錄。
    CONSTRAINT UQ_AccountVerifications_Url UNIQUE (url)
);

-- DROP TABLE IF EXISTS ResetPasswordVerifications;

CREATE TABLE ResetPasswordVerifications
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    url             VARCHAR(255) NOT NULL,
    expiration_date DATETIME NOT NULL, -- 表示這個欄位必須有一個日期時間值，不能為空。但是具體的值需要在插入資料時由程式或使用者指定。
    -- expiration_date（到期日期）通常不是「建立記錄的時間」，而是「未來某個時間點」。
    -- 例如：驗證連結可能在建立後 24 小時或 7 天後到期。
    -- 如果直接用 CURRENT_TIMESTAMP，就會變成「建立的當下時間」，而不是「到期時間」。
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_ResetPasswordVerifications_User_Id UNIQUE (user_id),
    CONSTRAINT UQ_ResetPasswordVerifications_Url UNIQUE (url)
);

-- DROP TABLE IF EXISTS TwoFactorVerifications;

CREATE TABLE TwoFactorVerifications
(
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT UNSIGNED NOT NULL,
    code            VARCHAR(10) NOT NULL,
    expiration_date DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES Users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT UQ_TwoFactorVerifications_User_Id UNIQUE (user_id),
    CONSTRAINT UQ_TwoFactorVerifications_Code UNIQUE (code)
    -- 設定 UNIQUE 可以保證每個驗證碼在資料庫裡只出現一次，驗證碼（例如 OTP 或二次驗證碼）是用來確認使用者身份的。
    -- 如果兩個使用者同時拿到相同的驗證碼，系統就無法分辨到底是哪一個使用者在驗證。
);