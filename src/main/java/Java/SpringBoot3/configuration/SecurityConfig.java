package Java.SpringBoot3.configuration;

import Java.SpringBoot3.filter.CustomAuthorizationFilter;
import Java.SpringBoot3.handler.CustomAccessDeniedHandler;
import Java.SpringBoot3.handler.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

//import java.beans.BeanProperty;

import java.util.Arrays;
import java.util.List;

import static Java.SpringBoot3.constant.Constants.PUBLIC_URLS;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;


/*npm i @auth0/angular-jwt
這個指令是用來安裝 Auth0 提供的 Angular JWT 套件。
套件的用途
    JWT (JSON Web Token) 驗證支援：
        這個套件提供一個 Angular 的 HttpInterceptor，可以在你用 HttpClient 發送請求時，自動把 JWT（通常存在 localStorage 或 cookie）附加到 HTTP Header 裡。
    簡化流程：
        你不用在每個 API 呼叫手動加上 Authorization: Bearer <token>，它會自動幫你處理。
    專注於 Token 使用：
        套件本身不負責「登入」或「取得 JWT」，它只負責在你已經有 Token 的情況下，幫你把 Token 附加到請求。登入流程要靠你自己的後端 API。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final BCryptPasswordEncoder encoder;
    //create @Bean for it
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserDetailsService userDetailsService;
    private final CustomAuthorizationFilter customAuthorizationFilter;
/*

為什麼你得到 401 UNAUTHORIZED ???????
    你的 /user/register API 被 Spring Security 當成受保護的路徑。
    當你在 Postman 呼叫時，沒有帶任何 Token 或登入狀態 → Security Filter 直接攔截，回傳：
    {
      "statusCode": 401,
      "status": "UNAUTHORIZED",
      "reason": "You need to log in to access this resource"
    }

    這代表請求 根本沒進到你的 Controller (UserResource.saveUser)，而是在 Security Filter Chain 就被拒絕了。

為什麼你預期 400 BAD_REQUEST ???
    你的預期邏輯是：
    呼叫 /user/register → 進到 UserResource.saveUser → 呼叫 userService.createUser(user)。
    如果 email 已存在 → Repository/Service 層拋出 SQLIntegrityConstraintViolationException。
    這個例外會被 HandleException.sQLIntegrityConstraintViolationException(...) 捕捉，回傳：
 {
    "timeStamp": "2026-01-...",
    "statusCode": 400,
    "status": "BAD_REQUEST",
    "reason": "Email already in use. Please use a different email and try again.",
    "developerMassage": "Email already in use. Please use a different email and try again."
}


為什麼結果不同？
    因為 Security 配置沒有把 /user/register 放到「公開路徑 (permitAll)」。
    所以請求被攔截在 Security Filter Chain → 直接回 401。
    只有當路徑允許匿名訪問時，請求才會進到 Controller → 才有機會觸發 400 BAD_REQUEST。
解決方式
在你的 Security 配置裡 (通常是 SecurityFilterChain)，要把 /user/register 加到公開路徑



2️⃣ SecurityConfig 的 PUBLIC_URLS:
    這是 Spring Security 的 授權配置，真正決定哪些路徑可以匿名訪問。
    目前你只放行了 /user/login/** 和 /user/verify/code/**。
    沒有放行 /user/register，所以 Spring Security 還是要求登入 → 回傳 401 UNAUTHORIZED。

3️⃣ 為什麼 /user/register 沒有生效？
    因為：
    在 Filter 層，你有設定 /user/register 為公開路徑。
    但在 SecurityConfig 層，PUBLIC_URLS 沒有包含 /user/register。
    SecurityConfig 的授權規則優先於 Filter → 所以最後還是被攔截。
 */



    /*
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf().disable().cors().disable();
            http.sessionManagement().sessionCreationPolicy(STATELESS);
            http.authorizeHttpRequests().requestMatchers(PUBLIC_URLS).permitAll(); //we don't want to block the login
            http.authorizeHttpRequests().requestMatchers(DELETE, "/user/delete/**").hasAnyAuthority("DELETE:USER");
            http.authorizeHttpRequests().requestMatchers(DELETE, "/customer/delete/**").hasAnyAuthority("DELETE:CUSTOMER");
            http.exceptionHandling().accessDeniedHandler(customAccessDeniedHandler).authenticationEntryPoint(customAuthenticationEntryPoint);
            http.authorizeHttpRequests().anyRequest().authenticated();
            http.addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
            return http.build();
            // can use lambda expressions instead of chaining the method calls.
        }
    */


/*
「Adds a CorsFilter to be used. If a bean by the name of corsFilter is provided, that CorsFilter is used. Else if corsConfigurationSource is defined, then that CorsConfiguration is used. Otherwise, if Spring MVC is on the classpath a HandlerMappingIntrospector is used.」
➡️ 意思：
     系統會加入一個 CorsFilter 來處理跨來源請求。
        如果 Spring 容器中有名為 corsFilter 的 Bean，則使用該 CorsFilter。
        如果沒有 corsFilter，但有定義 corsConfigurationSource，則使用該 CorsConfiguration。
        如果以上都沒有，但專案中有 Spring MVC，則會使用 HandlerMappingIntrospector 來處理 CORS。
 */

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
/*
是在 Spring Security 的設定中啟用並自訂 CORS (跨來源資源共享)。
📖 拆解說明 :
1. .cors(...)
        這是 Spring Security 提供的方法，用來啟用 CORS 支援。
        CORS 允許前端（例如 Angular、React）在不同網域下呼叫後端 API。
2. configure -> configure.configurationSource(...)
        這是一個 Lambda 表達式，用來進一步設定 CORS。
        configure 代表 CorsConfigurer，它可以接收一個 CorsConfigurationSource 物件。
3. corsConfigurationSource()
        這是一個方法（通常在你的設定類別裡定義），用來提供一個 CorsConfigurationSource。
        CorsConfigurationSource 會告訴 Spring Security：
        哪些網域可以存取（allowed origins）
        哪些 HTTP 方法允許（GET、POST、PUT、DELETE…）
        是否允許攜帶憑證（cookies、Authorization header）
        哪些 headers 可以被使用
📌 運作流程 :
        當有跨來源請求進來時，Spring Security 會透過這個設定來檢查是否允許。
        例如：前端在 http://localhost:4200，後端在 http://localhost:8080，這就是跨來源。
        如果 corsConfigurationSource() 有設定允許 http://localhost:4200，那麼前端就能成功呼叫後端 API。
*/
                .cors(configure -> configure.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPoint))
                .authorizeHttpRequests(request ->
                        request.requestMatchers(PUBLIC_URLS).permitAll() //we don't want to block the login
                                //.requestMatchers(OPTIONS).permitALL() //Not needed
                                .requestMatchers(DELETE, "/user/delete/**")
                                .hasAnyAuthority("DELETE:USER")
                                .requestMatchers(DELETE, "/customer/delete/**")
                                .hasAnyAuthority("DELETE:CUSTOMER")
                                .anyRequest().authenticated())
                /*
                addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                    => 在 Spring Security 的 UsernamePasswordAuthenticationFilter 之前，加入自訂的授權過濾器。
                    => 這樣可以先驗證 Token，再進行後續的身份驗證。
                 */
                .addFilterBefore(customAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {//宣告一個 Spring Bean，名稱為 corsConfigurationSource。回傳型別是 CorsConfigurationSource，用來定義跨來源資源共享（CORS）的規則。
        var corsConfiguration = new CorsConfiguration(); //建立一個新的 CorsConfiguration 物件，準備設定跨域規則。
        corsConfiguration.setAllowCredentials(true);//允許跨域請求時攜帶 憑證資訊（例如 Cookie、Authorization header）。如果設為 false，瀏覽器跨域請求就不能帶上這些敏感資訊。

        /*
     📌 差異說明 :
        (1) http://localhost:4200
            這是 Angular CLI 預設的開發伺服器端口。
            當你執行 ng serve 時，Angular 應用程式會在這個網址啟動。
            用來測試前端 UI。
        (2) http://localhost:3000
            常見於後端伺服器（例如 Spring Boot、Node.js/Express、NestJS 等）。
            你可能在 Spring Boot 或其他後端框架裡設定伺服器跑在 3000 port。
            用來提供 API 給前端呼叫。


=============================
Access to XMLHttpRequest at 'http://localhost:8080/user/update'
from origin 'http://localhost:4200' has been blocked by CORS policy:
No 'Access-Control-Allow-Origin' header is present on the requested resource.

=> 這個錯誤訊息代表 瀏覽器的同源政策 (Same-Origin Policy) 阻擋了你的前端 Angular (http://localhost:4200) 呼叫後端 Spring Boot API (http://localhost:8080/user/update)，因為後端沒有正確回應 CORS header。
=> 瀏覽器發現前端 (4200) 嘗試跨來源呼叫後端 (8080)，但後端回應中沒有 Access-Control-Allow-Origin 標頭，因此被阻擋。

📌 為什麼會發生 :
    Angular 前端跑在 http://localhost:4200。
    Spring Boot 後端跑在 http://localhost:8080。
    這是 跨來源請求 (Cross-Origin Request)。
    如果後端沒有設定 CORS，瀏覽器就會拒絕。
         */
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000", "http://SpringBoot3.org")); //設定允許的來源（Origin）。只有來自這些網址的請求才允許跨域存取。常見用法是允許前端開發環境（Angular 在 4200 port、React 在 3000 port）以及正式網站。
        //corsConfiguration.serAllowedOrigins(Arrays.asList("*"));//註解掉的程式碼，原本是允許所有來源（*）。這樣會比較寬鬆，但不安全，通常不建議在正式環境使用。


        //設定允許的 HTTP Header。
        // 例如 Authorization（JWT Token）、Content-Type、Accept 等。
        // 這些 header 在跨域請求時會被允許。
        corsConfiguration.setAllowedHeaders(Arrays.asList("Origin", "Access-Control-Allow-Origin", "Content-Type",
                "Accept", "Jwt-Token", "Authorization", "Origin", "Accept", "X-Requested-With",
                "Access-Control-Request-Method", "Access-Control-Request-Headers"));


        //設定允許前端程式可以讀取的 回應 Header。
        // 例如伺服器回傳的 Jwt-Token、Authorization、File-Name 等。
        // 如果沒有設定，瀏覽器可能會擋住前端存取這些 header。
        corsConfiguration.setExposedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Jwt-Token", "Authorization",
                "Access-Control-Allow-Origin", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "File-Name"));


        //設定允許的 HTTP 方法。
        // 包含常見的 CRUD 方法（GET、POST、PUT、PATCH、DELETE）以及 OPTIONS（CORS 預檢請求）。
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));


        //建立一個 UrlBasedCorsConfigurationSource，並將剛剛設定好的 corsConfiguration 套用到所有路徑（/**）。
        //最後回傳這個設定，讓 Spring Security 使用。
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        //DAO = Data Access Object（資料存取物件）。
        /*
        在 Spring Security 的 DaoAuthenticationProvider 中，DAO 的意思是：
            => 這個 Provider 會透過 UserDetailsService 去存取使用者資料（通常來自資料庫）。
            => 當使用者登入時，DaoAuthenticationProvider 會呼叫 UserDetailsService.loadUserByUsername()，取得使用者的帳號、密碼、角色等資訊。
            => 然後它會用 PasswordEncoder（例如 BCryptPasswordEncoder）來驗證密碼是否正確。
         */
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder);
        return new ProviderManager(authProvider);
        /*
        return new ProviderManager(authProvider);
            => 建立並回傳一個 ProviderManager，它是 AuthenticationManager 的具體實作。
            => ProviderManager 可以管理多個 AuthenticationProvider，這裡只傳入一個 DaoAuthenticationProvider。
            => 作用：當使用者登入時，ProviderManager 會委派給 DaoAuthenticationProvider 來驗證帳號與密碼。
         */
    }
}

/*
📖 為什麼要停用 CSRF 防護（常用於 REST API）？
CSRF (Cross-Site Request Forgery) 是一種攻擊方式，利用使用者已登入的 Session，偷偷發送惡意請求。
在傳統 Web 應用（有 Session、Cookie）中，CSRF 防護很重要。
但在 REST API 或 JWT 驗證 的情境下：
API 通常是 無狀態 (stateless)，不依賴 Session。
每次請求都必須攜帶 Token（例如 JWT），伺服器不會用 Cookie 自動識別使用者。
因此 CSRF 防護就不必要，反而會造成 API 呼叫困難。
👉 所以在 REST API 中，通常會停用 CSRF 防護。

----------------------------------------------------------------------------------------------

📖 為什麼要停用 CORS 限制（跨來源資源共享）？
CORS (Cross-Origin Resource Sharing) 是瀏覽器的一種安全機制，限制網頁跨來源呼叫 API。
在開發 REST API 時，前端（例如 React、Angular）和後端（Spring Boot）常常不在同一個網域。
如果 CORS 沒有正確配置，前端會被瀏覽器阻擋，無法呼叫後端 API。
在某些情況下，開發者會選擇 停用 CORS，或在程式裡自訂允許的來源。
👉 停用 CORS 限制的原因是：方便前端跨來源呼叫 API，避免瀏覽器阻擋。

----------------------------------------------------------------------------------------------

📖 總結
DAO 在 DaoAuthenticationProvider 中代表 Data Access Object，透過 UserDetailsService 從資料庫存取使用者資訊。
停用 CSRF 防護：因為 REST API 是無狀態的，不依賴 Session，所以不需要 CSRF。
停用 CORS 限制：讓前端可以跨來源呼叫後端 API，避免瀏覽器阻擋。
👉 簡單來說：這些設定是為了讓 REST API + JWT 驗證 的架構更簡單、可用。
 */