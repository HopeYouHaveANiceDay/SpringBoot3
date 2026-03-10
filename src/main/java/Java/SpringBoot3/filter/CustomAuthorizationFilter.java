package Java.SpringBoot3.filter;

import Java.SpringBoot3.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


import static Java.SpringBoot3.constant.Constants.*;
import static Java.SpringBoot3.utils.ExceptionUtils.processError;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/*
為什麼需要 CustomAuthorizationFilter
Spring Security 的工作流程：每個 HTTP 請求都會經過一連串 Filter，這些 Filter 負責驗證、授權、記錄等。
自訂授權邏輯：有時候內建的 Filter 不符合需求，例如要用 JWT Token 或自訂的 TokenProvider，就需要自己寫一個 Filter。
OncePerRequestFilter：確保每個請求只執行一次授權檢查，避免重複驗證。

 */

@Component // 表示這個類別會被 Spring 容器管理，成為一個 Bean。
@RequiredArgsConstructor // Lombok 註解，會自動生成建構子，並注入 final 欄位（這裡是 tokenProvider）。
@Slf4j // Lombok 註解， 提供 log 物件方便記錄日誌。


//宣告一個自訂的授權過濾器，繼承 Spring Security 的 OncePerRequestFilter，確保每個 HTTP 請求只會執行一次過濾。
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    //依賴注入的 TokenProvider，用來驗證與解析 Token。
    private final TokenProvider tokenProvider;

    //protected static final String TOKEN_KEY = "token";
    //protected static final String EMAIL_KEY = "email";

    @Override //覆寫 doFilterInternal 方法, 這是 OncePerRequestFilter 要求實作的核心方法，用來處理每個 HTTP 請求。
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter) throws ServletException, IOException {

        /*
        嘗試執行授權邏輯。
        呼叫 getRequestValues(request) 取得請求中的資訊（例如 email、token 等，目前尚未實作）。
        呼叫 getToken(request) 取得 Token（目前尚未實作）。
         */
        try {
            //Map<String, String> values = getRequestValues(request);
            String token = getToken(request);

            Long userId = getUserId(request);
            /*
            使用 tokenProvider 驗證 Token 是否有效。
            驗證依據是 email 與 token。
             */
            if (tokenProvider.isTokenValid(userId, token)) {

                //如果 Token 有效，則透過 tokenProvider 取得使用者的權限清單（角色、權限）。
                //List<GrantedAuthority> authorities = tokenProvider.getAuthorities(values.get(TOKEN_KEY));
                List<GrantedAuthority> authorities = tokenProvider.getAuthorities(token);
                //建立一個 Authentication 物件，代表使用者的身份與權限。
                Authentication authentication = tokenProvider.getAuthentication(userId, authorities, request);


                /*
                將 Authentication 放入 SecurityContextHolder。
                這樣 Spring Security 後續流程就能認定使用者已登入並具備相應權限。
                 */
                SecurityContextHolder.getContext().setAuthentication(authentication);


            /*
            如果 Token 無效，清除 SecurityContext，表示使用者未授權。
            註解解釋：因為 Token 無效，所以這些使用者不被認可。
            */
            } else {
                SecurityContextHolder.clearContext();
            }//because we know these users are not authorized. the token is not valid
            filter.doFilter(request, response);

        } catch (Exception exception) {
            log.error(exception.getMessage());
            processError(request, response, exception); //refresh token will not go through this processError method and doFilterInternal method
        }
    }

/*
refresh token go through shouldNotFilter method and @GetMapping("/refresh/token")
📌拆解說明 :
(1) shouldNotFilter(HttpServletRequest request)
        這是 Spring 提供的方法，用來判斷某個 HTTP 請求是否要跳過 Filter。
        如果回傳 true → 表示這個請求 不會進入 Filter。
        如果回傳 false → 表示這個請求 需要經過 Filter。
(2) 條件判斷
        request.getHeader(AUTHORIZATION) == null
            → 如果請求沒有帶 Authorization header，就不需要過濾。
        !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX)
            → 如果 Authorization header 沒有以指定的 Token 前綴 (例如 "Bearer ") 開頭，就不需要過濾。
        request.getMethod().equalsIgnoreCase(HTTP_OPTIONS_METHOD)
            → 如果請求方法是 OPTIONS，通常是瀏覽器的 CORS 預檢請求，這類請求不需要過濾。
        asList(PUBLIC_ROUTES).contains(request.getRequestURI())
            → 如果請求的路徑在公開路由清單 (PUBLIC_ROUTES) 裡，就不需要過濾。
        例如：/login, /register, /refresh , 這些公開 API。
(3) 整體邏輯
        只要符合以上任一條件，就回傳 true → 表示「不需要過濾」。
        否則回傳 false → 表示「需要過濾」。
*/
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(AUTHORIZATION) == null || !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) ||
                request.getMethod().equalsIgnoreCase(HTTP_OPTIONS_METHOD) || asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

/*
Access Modifier : Public, Protected, Private, Package (default)
📖 詳細說明
public
    最開放，任何其他類別都能存取。
    常用於 API 或需要被外部呼叫的方法。
protected
    允許同一個 package 的類別存取。
    允許不同 package 的子類別存取。
    常用於繼承結構中，讓子類別能使用父類別的成員。
private
    最嚴格，只能在宣告它的類別內使用。
    常用於封裝 (Encapsulation)，保護內部資料不被外部直接修改。
default (package-private)
    當你沒有寫任何修飾子時，預設就是 package-private。
    只能在同一個 package 裡存取，跨 package 就不能用了。
    常用於只在同一個模組內共享的程式碼。
 */
    /*
    宣告一個方法 getRequestValues。
        參數：HttpServletRequest request → 代表目前的 HTTP 請求物件。
        回傳型別：Map<String, String> → 一個字典結構，鍵和值都是字串。
     */
    //private Map<String, String> getRequestValues(HttpServletRequest request) {
    //return of(EMAIL_KEY, tokenProvider.getSubject(getToken(request), request), TOKEN_KEY, getToken(request));}
        /*
        使用 Map.of 建立一個不可變的 Map。
這個 Map 有兩個鍵值對：
    EMAIL_KEY → tokenProvider.getSubject(getToken(request), request)
        EMAIL_KEY 是一個常數鍵（應該在類別中定義）。
        值是透過 tokenProvider.getSubject(...) 取得的。
        getToken(request) 先從請求中取出 Token，再交給 tokenProvider 去解析，得到 Token 的「主體」（通常是使用者的 email 或帳號）。
    TOKEN_KEY → getToken(request)
        TOKEN_KEY 是另一個常數鍵。
        值是直接呼叫 getToken(request)，取得請求中的 Token。

這個方法的作用是：
    從 HTTP 請求中解析出 使用者的 email（或 Token 主體）以及 Token 本身。
    然後把這兩個資訊放進一個 Map，方便後續使用。
換句話說，它會回傳一個字典，內容大概像這樣：
{
  "EMAIL_KEY": "使用者的email",
  "TOKEN_KEY": "JWT Token字串"
}

總結
目的：把請求中的 Token 與解析出的使用者 email 打包成 Map。
用途：後續授權流程可以直接透過這個 Map 取得必要的資訊，而不用重複解析。
         */

/*
extract the user’s ID from the JWT token that comes with the request.

1. getToken(request)
Reads the JWT token from the incoming HTTP request (usually from the Authorization header).
Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...".

2. tokenProvider.getSubject(token, request)
Verifies the token (checks signature, expiration, claims).
Extracts the sub (subject) claim from the token, which you stored as the user’s ID when creating the token.
Returns it as a Long.

3.Why wrap it in getUserId(...)?
Convenience: Instead of repeating the logic everywhere, you centralize it in one method.
Readability: getUserId(request) clearly communicates the intent — “give me the user ID from this request.”
Reusability: Any part of the filter that needs the user ID can call this method.

📌 Why in CustomAuthorizationFilter?
******* The filter runs before protected endpoints are accessed. *******
******* It checks the request’s token, validates it, and extracts the user ID. *******
        With the user ID, the filter (or downstream code) can:
         => Load user details from the database.
            Set authentication in the SecurityContext.
            Enforce authorization rules.
 */
    private Long getUserId(HttpServletRequest request) {
        return tokenProvider.getSubject(getToken(request), request);
    }


    /*
    "Bearer " 前綴的作用就是告訴伺服器：這個授權資訊是 Token 型態的，請用 Token 驗證邏輯來處理。
    它是標準化的規範，避免混淆不同的授權方式。
     */
    private String getToken(HttpServletRequest request) {
        return ofNullable(request.getHeader(AUTHORIZATION))//從 HTTP 請求中取得 Authorization Header 的值。 例如可能是："Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6..."
                .filter(header -> header.startsWith(TOKEN_PREFIX)) //檢查 Header 是否以 "Bearer " 開頭。如果不是，就不會繼續往下處理。這通常用在 Optional 或 Stream API 中，確保只有符合格式的字串才會被處理。
                .map(token -> token.replace(TOKEN_PREFIX, EMPTY)).get(); //把字串中的 "Bearer " 前綴移除，只留下真正的 Token。 //例如：原始字串："Bearer abc123xyz" , 移除後："abc123xyz"
    }
} // why "EMPTY" need to import StringUtils.EMPTY (org.apache.commons.lang3) rather than org.springframework.http.HttpHeaders.EMPTY;
