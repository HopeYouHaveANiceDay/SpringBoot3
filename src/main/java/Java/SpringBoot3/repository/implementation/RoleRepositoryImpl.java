package Java.SpringBoot3.repository.implementation;

import Java.SpringBoot3.domain.Role;
import Java.SpringBoot3.exception.ApiException;
import Java.SpringBoot3.repository.RoleRepository;
import Java.SpringBoot3.rowmapper.RoleRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static Java.SpringBoot3.enumeration.RoleType.ROLE_USER;
import static Java.SpringBoot3.query.RoleQuery.*;

import static java.util.Map.*;
import static java.util.Objects.requireNonNull;

@Repository //用來標記這個類別是 資料存取層 (Repository Layer)，專門負責與資料庫互動。Spring 會自動把它註冊成 Bean，並在需要的地方注入使用。
@RequiredArgsConstructor //它會自動生成一個建構子，包含所有 final 欄位。// 因為有 private final NamedParameterJdbcTemplate jdbc; : 透過 依賴注入 (Dependency Injection) 把 jdbc 傳進來。
@Slf4j //它會自動生成一個 log 物件，方便在程式裡使用日誌紀錄。
public class RoleRepositoryImpl implements RoleRepository<Role> {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Role create(Role data) {
        return null;
    }

    @Override
    public Collection<Role> List() {
        log.info("Fetching all roles");
        try {
            return jdbc.query(SELECT_ROLES_QUERY, new RoleRowMapper());
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    } //(int page, int pageSize)

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role data) {
        return null;
    }

    @Override
    public Boolean delete(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        log.info("Adding role {} to user id: {}", roleName, userId);//在執行前記錄訊息：Adding role ROLE_USER to user id: 101. 方便除錯與追蹤系統操作。

        try {
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, of ("name", roleName), new RoleRowMapper());
            /*
            查詢角色 (jdbc.queryForObject) :
                    使用 SQL 查詢 (SELECT_ROLE_BY_NAME_QUERY) 根據 roleName 找出角色。
                    Map.of("roleName", roleName) → 把角色名稱綁定到 SQL 參數。
                    new RoleRowMapper() → 把查詢結果轉換成 Role 物件。
                    最後得到一個 Role 物件，裡面包含Role的 id。
             */
            jdbc.update(INSERT_ROLE_TO_USER_QUERY, of("userId", userId, "roleId", requireNonNull(role).getId()));
            /*
            插入角色與使用者關聯 (jdbc.update) :
                    使用 SQL (INSERT_ROLE_TO_USER_QUERY) 把 Role 指派給使用者。
                    Map.of("userId", userId, "roleId", role.getId()) → 把使用者 ID 和角色 ID 綁定到 SQL。
                    這樣就會在資料庫的「使用者角色關聯表」新增一筆紀錄。
             */
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No role found by name: " + ROLE_USER.name());

        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        log.info("Fetching role for user id: {}", userId);
        try {
            return jdbc.queryForObject(SELECT_ROLE_BY_ID_QUERY, of("id", userId), new RoleRowMapper());
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No role found by name: " + ROLE_USER.name());
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {
        log.info("Updating role for user id: {}", userId);
        try {
            Role role = jdbc.queryForObject(SELECT_ROLE_BY_NAME_QUERY, of("name", roleName), new RoleRowMapper());
            jdbc.update(UPDATE_USER_ROLE_QUERY, of("roleId", role.getId(), "userId", userId));
        } catch (EmptyResultDataAccessException exception) {
            throw new ApiException("No role found by name: " + roleName);
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }
}
