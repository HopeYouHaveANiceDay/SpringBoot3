
package Java.SpringBoot3.domain;
import Java.SpringBoot3.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import static Java.SpringBoot3.dtomapper.UserDTOMapper.fromUser;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
@RequiredArgsConstructor

/*
✅ 總結
UserPrincipal 是 Spring Security 的使用者模型。
✅✅✅它提供帳號、密碼、權限、狀態等資訊，讓框架能正確驗證與授權。
加上 getUser() 方法，可以直接把驗證後的使用者轉成 UserDTO，方便在 Controller 回傳給前端。
👉 一句話：UserPrincipal 是 Spring Security 與你的 User 實體之間的橋樑，✅讓登入驗證與授權流程能順利運作。
*/

/*
📖 登入流程回顧 :
    (1) 在 Postman 傳入：
            json
            {
              "email": "KatieFan123@gmail.com",
              "password": "123456"
            }
    (2) AuthenticationManager → 委派給 DaoAuthenticationProvider。
    (3) DaoAuthenticationProvider → 呼叫 UserDetailsService.loadUserByUsername(email)。
    (4) UserDetailsService → 回傳一個 UserPrincipal（你的自訂類別）。
    (5) Spring Security → 用 UserPrincipal.getPassword() 與 DB 的加密密碼比對，再檢查 getAuthorities()、isEnabled()、isAccountNonLocked() 等。
    (6) 如果任何一個檢查失敗 → 登入失敗，回傳 401 UNAUTHORIZED。
 */

/*
UserPrincipal：這是一個自訂的類別，用來包裝我們的 User 物件，並且實作 Spring Security 提供的 UserDetails 介面。
目的：讓 Spring Security 能透過這個類別讀取使用者的帳號、密碼、權限等資訊，進行驗證與授權。

📖 總結
UserPrincipal 是 Spring Security 的使用者模型，用來提供帳號、密碼、權限、狀態等資訊。
它透過實作 UserDetails 介面，讓 Spring Security 能夠：
    驗證使用者密碼 (getPassword())
    確認登入帳號 (getUsername())
    檢查帳號狀態（是否鎖定、是否啟用等）
    取得使用者的權限 (getAuthorities())
👉 簡單來說：這個類別是 Spring Security 與你的 User 實體之間的橋樑，讓框架能正確地進行登入驗證與授權。
 */
public class UserPrincipal implements UserDetails {

    // User user：代表系統中的使用者物件。
    private final User user;

    private final Role role;

    // String permissions：代表使用者的權限字串（例如 "READ:USER,DELETE:USER"）。
    //private final String permissions;


    /*
    getAuthorities()：回傳使用者的權限集合。
        => permissions.split(",")：把權限字串用逗號分割成陣列。
           例如：
           "DELETE:USER,READ:USER" → ["DELETE:USER", "READ:USER"]。

        => map(SimpleGrantedAuthority::new) → 把每個字串轉換成 SimpleGrantedAuthority 物件。
           例如：
           "DELETE:USER" → new SimpleGrantedAuthority("DELETE:USER")。

        => collect(toList())
           → 把所有物件收集成一個清單。

    👉 這樣 Spring Security 就能知道使用者擁有哪些權限。
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //return stream(role.getPermission().split(",".trim())).map(SimpleGrantedAuthority::new).collect(toList());
        //return stream(permissions.split(",".trim())).map(SimpleGrantedAuthority::new).collect(toList());

/*
public final class AuthorityUtils {
public static final List<GrantedAuthority> NO_AUTHORITIES = Collection.emptyList();
public static List<GrantedAuthority> commaSeparatedStringToAuthorityList(String authorityString) { return createAuthorityList(StringUtils.tokenizeToStringArray (authorityString, delimiters: ";"));

在 Spring Security 中，使用者通常會有一組 GrantedAuthority（例如 "ROLE_ADMIN"、"ROLE_USER"）。
但有些情況下，使用者可能沒有任何權限，這時候就可以直接回傳 AuthorityUtils.NO_AUTHORITIES，而不用每次都新建一個空清單。
 */
        return AuthorityUtils.commaSeparatedStringToAuthorityList(role.getPermission());
/*
📖 詳細解釋
1. @Override
    表示這個方法覆寫了 UserDetails 介面中的 getAuthorities() 方法。Spring Security 會透過這個方法取得使用者的權限。

2. ✅AuthorityUtils.commaSeparatedStringToAuthorityList(...)
    這是 Spring Security 提供的工具方法。
    ✅它會把一個以逗號分隔的字串（例如 "ROLE_ADMIN,ROLE_USER"）轉換成一個 List<GrantedAuthority>。
    每個字串都會包裝成 SimpleGrantedAuthority 物件，讓 Spring Security 能識別。

3. role.getPermission()
    這裡假設 Role 物件有一個欄位 permission，存放使用者的權限字串。
    例如："READ:USER,DELETE:USER" 或 "ROLE_ADMIN,ROLE_USER"。

4. 回傳值
    最後回傳的是一個 Collection<? extends GrantedAuthority>，代表使用者的所有權限。
    Spring Security 會用這些權限來判斷使用者是否能存取某些資源。

✅ 總結
    這段程式碼的作用是：
    把角色中的權限字串轉換成 Spring Security 能理解的 GrantedAuthority 集合，讓框架在授權時能正確判斷使用者的權限。
    👉 一句話：getAuthorities() 就是把 "ROLE_ADMIN,ROLE_USER" 這樣的字串，轉成 Spring Security 的權限物件清單。
*/

    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }//getPassword()：回傳使用者的密碼。

    @Override //回傳使用者的帳號名稱。這裡使用 email 當作登入帳號。
    public String getUsername() {
        return user.getEmail();
    }//getUsername()：回傳使用者的帳號名稱，這裡用 email 當作登入帳號。

    @Override //isAccountNonExpired() → 帳號是否未過期，這裡固定回傳 true。
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override //isAccountNonLocked() → 帳號是否未被鎖定，依照 user.isNotLocked() 判斷。
    public boolean isAccountNonLocked() {
        return user.isNotLocked();
    }

    @Override //isCredentialsNonExpired() → 憑證（密碼）是否未過期，這裡固定回傳 true。
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override //isEnabled() → 帳號是否啟用，依照 user.isEnabled() 判斷。
    public boolean isEnabled() {
        return user.isEnabled();
    }

    //在 UserPrincipal 裡，你定義了一個方法 getUser()，用來把內部的 User 轉換成 UserDTO。
    public UserDTO getUser() {//return userDTO
        return fromUser(user, role);//we have a way to get the user from the authentication
        /*
        這段程式碼之所以需要加在 UserPrincipal.java，是因為在 Spring Security 的驗證流程裡，
        UserPrincipal 代表了「已驗證的使用者」。
        如果你想在 Controller 或 Service 中方便地取得使用者資訊（例如 email、角色、權限），
        就需要提供一個方法把 UserPrincipal 轉換成你自己的 UserDTO。

📖 為什麼要加 getUser() 方法
(1) Spring Security 的 Authentication 機制
    當使用者登入成功後，Spring Security 會建立一個 Authentication 物件。
    =這個物件的 principal 通常就是你自訂的 UserPrincipal。
    如果你只用 principal，拿到的是 UserPrincipal，但前端或 API 回應通常需要的是 UserDTO。
(2) DTO 的用途
    UserDTO 是你設計的資料傳輸物件，用來回傳給前端。
    它通常只包含必要的欄位（例如 email、角色），而不會包含敏感資訊（例如密碼）。
    所以需要一個方法把 UserPrincipal → UserDTO。
(3) 方便 Controller 使用
    在 @GetMapping("/profile") 裡，你會拿到 Authentication authentication。
    authentication.getPrincipal() → 其實就是 UserPrincipal。
    如果 UserPrincipal 有 getUser() 方法，就能直接轉成 UserDTO，方便回傳。

    👉 這樣就不用再呼叫 userService.getUserByEmail(authentication.getName())，因為 UserPrincipal 已經有使用者資訊了
✅ 一句話總結：
在 UserPrincipal 裡加 getUser() 方法，是為了把驗證後的使用者資訊直接轉成 UserDTO，方便 Controller 回傳給前端，避免重複查詢資料庫。

            @GetMapping("/profile") // Authentication -> CustomAuthorizationFilter.java
                public ResponseEntity<HttpResponse> profile(Authentication authentication) {


                    //UserDTO user = userService.getUserByEmail(authentication.getName()); //this name should be the email.
                    // return ResponseEntity.ok().body(

***** 在 UserPrincipal 裡加 getUser() 方法，可以直接把驗證後的使用者轉成 UserDTO，讓 Controller 更簡潔，不需要再查一次資料庫。
                    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                    UserDTO user = principal.getUser(); // 直接拿到 DTO


                            HttpResponse.builder()
                                    .timeStamp(now().toString())
                                    .data(of("user", user))
                                    .message("Profile Retrieved")
                                    .status(OK)
                                    .statusCode(OK.value())
                                    .build());
                }
         */
    }
}

