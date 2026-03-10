package Java.SpringBoot3.resource;
import Java.SpringBoot3.domain.UserPrincipal;

import Java.SpringBoot3.event.NewUserEvent;
import Java.SpringBoot3.exception.ApiException;
import Java.SpringBoot3.form.*;
import Java.SpringBoot3.provider.TokenProvider;
import Java.SpringBoot3.repository.UserRepository.*;
import Java.SpringBoot3.domain.HttpResponse;
import Java.SpringBoot3.domain.User;
import Java.SpringBoot3.dto.UserDTO;
import Java.SpringBoot3.service.EventService;
import Java.SpringBoot3.service.RoleService;
import Java.SpringBoot3.service.UserService;

//import Java.SpringBoot3.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static Java.SpringBoot3.constant.Constants.TOKEN_PREFIX;
import static Java.SpringBoot3.dtomapper.UserDTOMapper.toUser;
import static Java.SpringBoot3.enumeration.EventType.*;
import static Java.SpringBoot3.utils.ExceptionUtils.processError;
import static Java.SpringBoot3.utils.UserUtils.getAuthenticatedUser;
import static Java.SpringBoot3.utils.UserUtils.getLoggedInUser;
import static java.time.LocalDateTime.now;
import static java.util.Map.*;
//import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;


@RestController//宣告這是一個 RESTful Controller，回傳的物件會自動轉換成 JSON。
@RequestMapping(path = "/user")//設定基礎路徑為 /user，所有方法的路徑都會以此為前綴。
@RequiredArgsConstructor//Lombok 註解，會自動建立建構子，並注入 final 欄位的依賴。
public class UserResource {

    //📖 依賴注入
    //private static final String TOKEN_PREFIX = "Bearer ";
    private final UserService userService; //UserService → 處理使用者相關邏輯 (建立、查詢)。
    private final RoleService roleService; //RoleService → 處理角色與權限。
    private final EventService eventService;
    private final AuthenticationManager authenticationManager;//AuthenticationManager → Spring Security 的驗證管理器，用來檢查帳號密碼是否正確。
    private final TokenProvider tokenProvider;//TokenProvider → 負責建立與驗證 JWT Token。
    private final HttpServletRequest request;
    private final ApplicationEventPublisher publisher;
    /*
    HttpServletRequest 是 Java Servlet API 提供的一個介面 (interface)。
    它代表 客戶端送到伺服器的 HTTP 請求。
      => 透過這個物件，你可以取得請求的各種資訊，例如：
            請求方法 (GET、POST…)
            請求的 URL 路徑
            Query String 或表單參數
            Header（標頭資訊）
            Cookie
            Body（請求內容）

    request 是 參數名稱，你可以隨意取名（例如叫 req 也可以），但慣例上常用 request。
    它的型別是 HttpServletRequest，所以你可以呼叫 request.getMethod()、request.getRequestURI() 等方法來取得請求資訊。
     當 Servlet 或 Spring MVC 呼叫這個方法時，會把目前的 HTTP 請求物件傳進來。

     📖 總結:
        HttpServletRequest → 一個介面，代表 HTTP 請求。
        request → 方法的參數名稱，型別是 HttpServletRequest，代表目前的請求物件。
        用途 → 讓你在程式裡讀取客戶端送來的資料（路徑、參數、header、body…）。
     */
    private final HttpServletResponse response;
        /*
    為什麼要用 ResponseEntity ?
      => 在 Spring MVC 或 Spring Boot 的 Controller 中，通常會回傳物件給前端。
      => 如果只回傳物件，Spring 會自動包裝成 JSON，狀態碼預設是 200 OK。
      => 但有時候我們需要更精細的控制，例如：
            指定不同的 HTTP 狀態碼。
            加上自訂的 HTTP 標頭。
            回傳不同格式的資料。
     */
    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm) {
        UserDTO user = authenticate(loginForm.getEmail(), loginForm.getPassword());
        //UserDTO user = getLoggedInUser(authentication);
        //System.out.println(authentication); //UsernamePasswordAuthenticationToken 繼承了 AbstractAuthenticationToken，而後者覆寫了 toString() → 所以輸出完整資訊。
        //System.out.println(((UserPrincipal) authentication.getPrincipal()).getUser()); // UserDTO 沒有覆寫 toString() → 所以只輸出類別名稱 + 位址。
        return  user.isUsingMfa() ? sendVerificationCode(user) : sendResponse(user); //if the user is using Mfa, we send the verification code, otherwise, we send the response.
    }

/*
we can get userDTO from the authentication => UserPrincipal.java
(1) 參數 Authentication authentication
    這是 Spring Security 在使用者登入後，存放於 SecurityContext 的物件。
    裡面包含了使用者的身份資訊 (principal)、授權資訊 (authorities)、以及是否已驗證。

(2) authentication.getPrincipal()
    取得「使用者的主要身份資訊」。
    在你的專案裡，這個 principal 是自訂的 UserPrincipal。
    UserPrincipal 通常包裝了你的 User 實體，並實作了 UserDetails 介面。

(3) (UserPrincipal) authentication.getPrincipal()
    把 principal 強制轉型成 UserPrincipal，因為你知道它就是這個型別。

(4) .getUser()
    在 UserPrincipal 裡，你定義了一個方法 getUser()，用來把內部的 User 轉換成 UserDTO。
    這樣就能避免直接把 User 實體（可能包含敏感資訊，例如密碼）暴露給前端。


import UserUtils.java to replace the following code :
    private UserDTO getAuthenticatedUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }
*/

/*
📖 說明
(1) 方法名稱：authenticate
    這是一個私有方法，用來執行使用者的登入驗證。
    參數是 email 和 password，代表使用者輸入的帳號與密碼。
(2) authenticationManager.authenticate(...)
    authenticationManager 是 Spring Security 提供的 驗證管理器。
    它的作用是接收一個「未驗證的憑證 (Authentication)」，然後去檢查是否正確。
    如果驗證成功，會回傳一個「已驗證的 Authentication 物件」。
(3) unauthenticated(email, password)
    這通常是一個方法，用來建立「未驗證的 Authentication 物件」，裡面包含使用者輸入的 email 和 password。
    例如可能建立一個 UsernamePasswordAuthenticationToken(email, password)。
    這個物件會交給 authenticationManager 去驗證。
(4) 回傳值：Authentication
    驗證成功後，authenticationManager 會回傳一個「已驗證的 Authentication 物件」。
    這個物件裡面包含使用者的身份資訊 (principal)、授權資訊 (authorities)，可以用來後續產生 JWT 或存放在 SecurityContext。
         */


    @PostMapping("/register")                 // @RequestBody → 把前端傳來的 JSON 轉換成 User 物件。
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user) throws InterruptedException { //name the method saveUser
        TimeUnit.SECONDS.sleep(4);
        UserDTO userDto = userService.createUser(user);
         //.body(...) → 設定回應主體。
         return ResponseEntity.created(getUri()).body(
                 HttpResponse.builder()
                         .timeStamp(now().toString())
                         .data(of("user", userDto)) //data(of("user", userDto)) → 把建立好的使用者 DTO 放進回應資料。
                         .message(String.format("User account created for user %s", user.getFirstName())) //message("User created") → 訊息文字，表示使用者建立成功。
                         .status(CREATED) //status(HttpStatus.CREATED) → 設定 HTTP 狀態為 201 Created。
                         .statusCode(CREATED.value()) //statusCode(HttpStatus.CREATED.value()) → 設定狀態碼數字 201。
                         .build());
         /*
         POST : http://localhost:8080/user/register
         {
                "email": "john@gmail.com",
                "password": "123456",
                "firstName": "Chan",
                "lastName": "Smith"
         }

         Output :
         {
            "timeStamp": "2026-02-...",
            "statusCode": 201,
            "status": "CREATED",
            "message": "User created",
            "data": {
                "user": {
                    "id": 5,
                    "firstName": "Chan",
                    "lastName": "Smith",
                    "email": "john@gmail.com",
                    "address": null,
                    "phone": null,
                    "title": null,
                    "bio": null,
                    "imageUrl": null,
                    "enabled": false,
                    "createdAt": null,
                    "roleName": "ROLE_USER",
                    "permissions": "READ:USER,READ:CUSTOMER",
                    "notLocked": true,
                    "usingMfa": false
                }
            }
        }
          */
    }

    /*
    (1) 為什麼要帶 Auth Token
        安全性：/user/profile 是一個受保護的 API，只有登入並取得 Token 的使用者才能存取。
        身份驗證：Token（通常是 JWT）裡面包含使用者的身份資訊，後端會用它來判斷你是誰、是否有權限。
        避免匿名存取：如果沒有 Token，後端會拒絕請求，因為 Profile 屬於私人資料。

    (2) 為什麼要呼叫 /user/profile
        取得使用者資訊：這個 API 會回傳使用者的詳細資料（例如 id、姓名、email、角色、權限）。
        驗證授權流程：呼叫這個 API 可以確認 Token 驗證是否正確，SecurityFilterChain 與 CustomAuthorizationFilter 是否正常工作。
        前端需求：在實際應用中，前端登入後需要顯示使用者的個人資料，這個 API 就是提供資料的來源。
     */
    @GetMapping("/profile") // Authentication -> CustomAuthorizationFilter.java
    public ResponseEntity<HttpResponse> profile(Authentication authentication) {
        UserDTO user = userService.getUserByEmail(getAuthenticatedUser(authentication).getEmail());//這樣就能直接拿到登入者的 UserDTO，而不用每次都自己轉型。
        //If we want to see what actually done in authentication
        //remove-> no need the log ->System.out.println(authentication); // authentication.getPrincipal() -> the Principal is the email
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "events", eventService.getEventsByUserId(user.getId()), "roles", roleService.getRoles())) //this user is UserDTO //should include user event and the role in database if they have the permission to change their role
                        .message("Profile Retrieved")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    // @PatchMapping 注解用于将HTTP PATCH 请求映射到指定的处理方法。HTTP PATCH 方法通常用于更新资源的部分内容，而不是替换整个资源。
    @PatchMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateForm user) {
        //TimeUnit.SECONDS.sleep(3);
        UserDTO updatedUser = userService.updateUserDetails(user);
        publisher.publishEvent(new NewUserEvent(updatedUser.getEmail(), PROFILE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", updatedUser, "events", eventService.getEventsByUserId(user.getId()), "roles", roleService.getRoles()))
                        .message("User updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    /*
    這段程式碼是一個 Spring Boot Controller 方法，用來處理使用者驗證碼的 API 請求。
    @GetMapping("/verify/code/{email}/{code}")
        => 宣告這是一個 GET API，路徑格式為：/verify/code/{email}/{code}
    {email} 與 {code} 是路徑參數 (Path Variable)，會由使用者輸入。

    @PathVariable("email") String email
    => 把 URL 中的 {email} 參數綁定到方法的 email 變數。

    @PathVariable("code") String code
    => 把 URL 中的 {code} 參數綁定到方法的 code 變數。
     */
    @GetMapping("/verify/code/{email}/{code}") //http://localhost:8080/user/verify/code/KatieFan@gmail.com/DLOJFKCR
    public ResponseEntity<HttpResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {

        /*
        📖 呼叫 Service 驗證 :
           UserDTO user = userService.verifyCode(email, code);
        呼叫 userService.verifyCode(email, code)，檢查 Email 與驗證碼是否匹配。
        如果驗證成功，會回傳一個 UserDTO 物件，代表使用者的基本資訊。
         */
        UserDTO user = userService.verifyCode(email, code); //call userService to verify email and code

        //1. 為什麼要用 user.getEmail()?
        //    => 事件需要知道「是哪個使用者」觸發的。user.getEmail() 是唯一識別使用者的關鍵資訊，方便在 UserEvents 表裡記錄。
        //2. 為什麼要用 LOGIN_ATTEMPT_SUCCESS?
        //     => 事件類型必須明確標示「這次事件是什麼」。在這裡，使用者輸入驗證碼成功 → 代表登入成功。所以用 EventType.LOGIN_ATTEMPT_SUCCESS，讓資料庫和監聽器知道這是一個「登入成功」事件。
        publisher.publishEvent(new NewUserEvent(user.getEmail(), LOGIN_ATTEMPT_SUCCESS));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user))
                            , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(user))))
                        /*
                        📖 getUserPrincipal(user) 的作用 :
                                getUserPrincipal(user) 是一個方法，用來把 UserDTO 轉換成 UserPrincipal。
                            為什麼需要這樣做？
                                UserDTO → 是一個資料傳輸物件 (Data Transfer Object)，通常只包含前端需要的使用者資訊。
                                UserPrincipal → 是 Spring Security 中的「使用者主體」，封裝了完整的使用者資料與權限，用來支援安全驗證與授權。
                         */
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }


//START - To reset password when user is not logged in
//START - To reset password when user is not logged in
//START - To reset password when user is not logged in
    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email) {//get the email of a user
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Email sent. Please check your email to reset your password.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }


    @GetMapping("/verify/account/{key}") // key => random UUID
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable("key") String key) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        // UserDTO user = userService.verifyAccountKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message(userService.verifyAccountKey(key).isEnabled() ? "Account already verified" : "Account verified")
                        /*
                        (1) userService.verifyAccountKey(key)
                            呼叫 Service 層的方法，用 key (random UUID) 去驗證帳號。
                            這個方法通常會回傳一個 User 物件。
                        (2) .isEnabled()
                            檢查該 User 是否已經啟用（enabled）。
                            如果帳號已經驗證過，isEnabled() 會是 true。
                        (3) 三元運算子 ? :
                            語法：條件 ? 值1 : 值2。
                            如果條件為 true → 使用 值1。
                            如果條件為 false → 使用 值2。
                        (4) 結果
                            如果 isEnabled() 為 true → 訊息是 "Account already verified"。
                            如果 isEnabled() 為 false → 訊息是 "Account verified"。
                         */
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
        /*
        GET : http://localhost:8080/user/verify/account/edd6656b-9dc9-45e1-b3a5-613b104bbe09
        Output :
        {
            "timeStamp": "2026-02-...",
            "statusCode": 200,
            "status": "OK",
            "message": "Account verified"
        }

        click "send" again
        {
            "timeStamp": "2026-02-...",
            "statusCode": 200,
            "status": "OK",
            "message": "Account already verified"
        }
         */
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyPasswordUrl(@PathVariable("key") String key) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        //throw new ApiException("Some error occurred - error from the back end");
        UserDTO user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user)) //pass the user as the data. Use user for the next request
                        .message("Please enter a new password")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
        /*
        {
            "timeStamp": "2026-02-....",
            "statusCode": 200,
            "status": "OK",
            "message": "Please enter a new password",
            "data": {
                "user": {
                    "id": 4,
                    "firstName": "Katie",
                    "lastName": "Fan",
                    "email": "KatieFan123@gmail.com",
                    "address": null,
                    "phone": "",
                    "title": null,
                    "bio": null,
                    "imageUrl": "https://cdn-icons-png.flaticon.com/512/149/149071.png",
                    "enabled": true,
                    "createdAt": "2026-01-...",
                    "roleName": "ROLE_USER",
                    "permissions": "READ:USER,READ:CUSTOMER",
                    "notLocked": true,
                    "usingMfa": false
                }
            }
        }
         */
    }


    @PutMapping("/new/password")
    public ResponseEntity<HttpResponse> resetPasswordWithKey(@RequestBody @Valid NewPasswordForm form) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        userService.updatePassword(form.getUserId(), form.getPassword(), form.getConfirmPassword());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .message("Password reset successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
        /*
        http://localhost:8080/user/resetpassword/23a3ab8d-dac5-4c06-a537-ff8f8693a89e/789456/789456
        => 789456 (reset password)
        => 789456 (reset confirmPassword)
        {
            "timeStamp": "2026-02-03T21:24:36.711374",
            "statusCode": 200,
            "status": "OK",
            "message": "Password reset successfully"
        }

        用原本的password:
        POST
        {
            "email": "KatieFan123@gmail.com",
            "password": "111111"
        }
        {
            "timeStamp": "2026-02-03T21:28:31.715089",
            "statusCode": 400,
            "status": "BAD_REQUEST",
            "reason": "Bad credentials"
        }
         */
    }

//END - To reset password when user is not logged in
//END - To reset password when user is not logged in
//END - To reset password when user is not logged in


/*
這是一個 REST API，用來更新使用者密碼。
它接收：
    Authentication authentication → Spring Security 提供的物件，代表目前已登入的使用者。
    UpdatePasswordForm form → 前端送來的表單，包含舊密碼、新密碼、確認新密碼。
    呼叫 getAuthenticatedUser(authentication) 來取得目前登入的使用者資訊 (UserDTO)。
    然後呼叫 userService.updatePassword(...) 執行密碼更新。

📌 關聯性
在 updatePassword 方法裡，呼叫 getAuthenticatedUser(authentication) → 實際上就是呼叫 UserUtils.getAuthenticatedUser(authentication)。
這樣就能從 Authentication 物件中取出目前登入的使用者 (UserDTO)。
取得使用者之後，就能知道 是哪個使用者要更新密碼，並呼叫 userService.updatePassword(...)。
     */
    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordForm form) {
        // we need (Authentication authentication), so we can know who (users) sends the request
// @Override protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
    UserDTO userDTO = getAuthenticatedUser(authentication);
    userService.updatePassword(userDTO.getId(), form.getCurrentPassword(), form.getNewPassword(), form.getConfirmNewPassword());
    publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), PASSWORD_UPDATE));
    return ResponseEntity.ok().body(
            HttpResponse.builder()
                    .timeStamp(now().toString())
                    //we need the user and the roles
                    //這樣就會得到一個 Map，裡面有兩個 key-value pair：
                    //這段程式碼的作用是：在回應裡包含兩個主要資料欄位：
                    //user → 使用者的詳細資訊（例如 id、email、profile 等）。
                    //roles → 使用者的角色清單（例如 ADMIN, USER）。
                    //這行的目的，是把使用者資訊和角色清單打包成一個 Map，放進回應物件裡，讓前端在登入成功後能同時拿到使用者資料和角色資訊。
                    .data(of("user", userService.getUserById(userDTO.getId()), "events", eventService.getEventsByUserId(userDTO.getId()), "roles", roleService.getRoles()))
                    .message("Password updated successfully")
                    .status(OK)
                    .statusCode(OK.value())
                    .build());
    }

    //in VS code, this.profileState$ = this.userService.updateRoles$(roleForm.value.roleName)
/*   1. @PatchMapping("/update/role/{roleName}")
            這是一個 Spring MVC 的註解，表示這個方法會處理 HTTP PATCH 請求，路徑為 /update/role/{roleName}。
            {roleName} 是路徑參數，代表要更新的角色名稱。 */
    @PatchMapping("/update/role/{roleName}")

/*      2. 方法簽名
            public ResponseEntity<HttpResponse> updateUserRole(...)
            方法回傳一個 ResponseEntity，裡面包裝自訂的 HttpResponse 物件。
            ResponseEntity 是 Spring 提供的標準 HTTP 回應封裝，可以設定狀態碼、回應內容。
        3. 參數
            Authentication authentication：Spring Security 提供的物件，代表目前已登入的使用者。
            @PathVariable("roleName") String roleName：從 URL 路徑中取出角色名稱，例如 /update/role/ROLE_ADMIN。 */
    public ResponseEntity<HttpResponse> updateUserRole(Authentication authentication, @PathVariable("roleName") String roleName) {

        /*      4. 取得目前登入的使用者
            呼叫自訂方法 getAuthenticatedUser，從 Authentication 物件中解析出使用者資訊，並轉成 UserDTO。 */
        UserDTO userDTO = getAuthenticatedUser(authentication);

        /* 5. userService.updatedUserRole(userDTO.getId(), roleName);
                    這行程式碼的作用是 更新資料庫裡使用者的角色。
                    它會根據目前登入使用者的 ID (userDTO.getId()) 和傳入的角色名稱 (roleName)，去修改使用者的角色紀錄。
                    沒有這一步，資料庫裡的角色不會改變。
                */
        userService.updatedUserRole(userDTO.getId(), roleName);
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), ROLE_UPDATE));


/*
6. data(of("user", userService.getUserById(userDTO.getId()), "roles", roleService.getRoles()))
        這行程式碼的作用是 建立回應資料，包含：
        "user"：呼叫 userService.getUserById(...) 取得使用者最新的資料。
        "roles"：呼叫 roleService.getRoles() 取得系統中所有角色。
=======================================================

1. 兩者的關係
     (1) 如果沒有執行 updatedUserRole(...)：
            getUserById(...) 仍然可以回傳使用者資料，但角色欄位會是舊的，沒有更新。
     (2) 換句話說，你還是能呼叫 .data(...)，但回傳的使用者角色不會改變。
            如果有執行 updatedUserRole(...)：
            資料庫裡的角色已經更新。
            接著再呼叫 getUserById(...)，就能拿到最新的角色資訊。
            這樣 .data(...) 回傳的 JSON 才會顯示更新後的角色。

2. 總結 :
    updatedUserRole(...) → 動作：修改資料庫裡的角色。
    getUserById(...) → 查詢：讀取資料庫裡的使用者資訊。
    .data(...) → 回應：把查詢到的使用者和角色集合包裝成回應。
    👉 所以，你可以單獨呼叫 .data(...) 而不更新角色，但那樣只會拿到舊資料。
    要讓回應裡的 "user" 角色是最新的，就必須先執行 updatedUserRole(...)。

=======================================================
1. 更新使用者角色
        java
        userService.updatedUserRole(userDTO.getId(), roleName);
        這行程式碼會呼叫 Service 層，去修改資料庫裡對應使用者的角色紀錄。
        例如：把 user_id=4 的角色更新成 "ROLE_ADMIN"。
        沒有這一步，資料庫裡的角色資訊不會改變。
2. 查詢最新的使用者資料
        java
        userService.getUserById(userDTO.getId())
        這行程式碼會再去資料庫查詢該使用者的最新資訊。
        因為前一步已經更新了角色，所以這裡查到的 user 物件就會包含最新的角色。
        然後把這個最新的使用者資料和所有角色集合 (roleService.getRoles()) 一起包裝到回應裡。
        兩者的關係
        updatedUserRole(...) → 動作：更新資料庫。
        getUserById(...) → 查詢：讀取資料庫。
        先更新再查詢 → 保證回傳的使用者角色是最新的。
        如果你只執行 getUserById(...) 而沒有先 updatedUserRole(...)，那麼回傳的使用者角色會是舊的，沒有更新。
✅ 總結
    後端 Spring Boot 先用 userService.updatedUserRole(...) 更新資料庫裡的角色，再用 userService.getUserById(...) 查詢最新的使用者資料，確保回應給前端的 JSON 是最新的角色資訊。

============================
前端網站之所以能拿到「最新的使用者角色資訊」，流程是這樣的：
    1. 前端送出請求
        當使用者在網頁上選擇新角色並提交表單時，前端會呼叫後端的 API，例如：
        Code
        PATCH /update/role/ROLE_ADMIN
        這個請求會帶上角色名稱（ROLE_ADMIN）以及使用者的身份驗證資訊（通常是 JWT token 或 session）。
    2. 後端更新資料庫
        後端 Controller 收到請求後，執行：
        java
        userService.updatedUserRole(userDTO.getId(), roleName);
        這一步會更新資料庫裡該使用者的角色紀錄。
    3. 後端查詢最新資料並回傳
        更新完成後，後端再呼叫：
        java
        userService.getUserById(userDTO.getId())
        這會重新查詢資料庫，拿到「最新的使用者資料」，其中角色欄位已經更新。
        然後後端把這個最新的使用者資料和所有角色集合 (roleService.getRoles()) 包裝成 JSON 回傳給前端。
    4. 前端接收最新 JSON
        前端收到後端回傳的 JSON，例如：
        json
        {
          "timeStamp": "2026-02-18T07:50:00",
          "statusCode": 200,
          "status": "OK",
          "message": "Role update successfully",
          "data": {
            "user": {
              "id": 4,
              "firstName": "Katie",
              "roleName": "ROLE_ADMIN"
            },
            "roles": [
              {"id":1,"name":"ROLE_USER"},
              {"id":2,"name":"ROLE_MANAGER"},
              {"id":3,"name":"ROLE_ADMIN"},
              {"id":4,"name":"ROLE_SYSADMIN"}
            ]
          }
        }
        前端就能用這個最新的 JSON 更新畫面，顯示使用者的新角色。
✅ 總結 :
    更新角色：userService.updatedUserRole(...) → 修改資料庫。
    查詢最新資料：userService.getUserById(...) → 從資料庫拿最新的使用者角色。
    回傳 JSON：後端把最新資料送回前端。
    前端更新畫面：前端用回傳的 JSON 顯示最新角色。
        */
        return  ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(of("user", userService.getUserById(userDTO.getId()), "events", eventService.getEventsByUserId(userDTO.getId()), "roles", roleService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Role update successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

/* in SettingsForm.java:
    @Getter
    @Setter
    public class SettingsForm {
        @NotNull(message = "Enabled cannot be null or empty")
        private Boolean enabled;
        @NotNull(message = "Not Locked cannot be null or empty")
        private Boolean notLocked;
    } */
    //後端：驗證表單 → 更新資料庫 → 查詢最新使用者 → 回傳 JSON。
//前端：收到回應後更新畫面，顯示最新的帳號狀態。
    @PatchMapping("/update/settings")
    //@RequestBody @Valid SettingsForm form：
    //   => 接收前端送來的 JSON，並用 SettingsForm 對應。
    //   => @Valid 會觸發欄位驗證。
    public ResponseEntity<HttpResponse> updateAccountSettings(Authentication authentication, @RequestBody @Valid SettingsForm form) {
        UserDTO userDTO = getAuthenticatedUser(authentication);//從登入的 Authentication 物件中解析出使用者資訊。

        //呼叫 Service 層，更新資料庫裡該使用者的帳號狀態（啟用/未鎖定）。
        userService.updateAccountSettings(userDTO.getId(), form.getEnabled(), form.getNotLocked());
        publisher.publishEvent(new NewUserEvent(userDTO.getEmail(), ACCOUNT_SETTINGS_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(of("user", userService.getUserById(userDTO.getId()), "events", eventService.getEventsByUserId(userDTO.getId()), "roles", roleService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Account settings updated successfully")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PatchMapping("/togglemfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication) throws InterruptedException {
        TimeUnit.SECONDS.sleep(3);
        UserDTO user = userService.toggleMfa(getAuthenticatedUser(authentication).getEmail());
        publisher.publishEvent(new NewUserEvent(user.getEmail(), MFA_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .data(of("user", user, "events", eventService.getEventsByUserId(user.getId()), "roles", roleService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Multi-Factor Authentication updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }


/*
前後端流程
    1. 前端：使用者在 Profile 頁面選擇圖片並提交。
        送出 PATCH /update/image，body 包含 MultipartFile。
    2. 後端 Controller：接收請求，呼叫 Service 的 updateImage(...)。
    3. Service 層：把圖片交給 Repository，更新資料庫。
    4. Repository 層：存檔並更新使用者紀錄。
    5. 回傳 JSON：包含最新的使用者資訊和成功訊息。
    6. 前端：收到回應後更新畫面，顯示新圖片。     */
    @PatchMapping("/update/image")
    /*
    在 Spring Security 裡，Authentication 物件就是用來代表 目前登入的使用者身份。
    它是整個安全框架的核心之一，當使用者成功登入後，
    Spring Security 會把使用者的認證資訊存放在 SecurityContext 裡，
    而 SecurityContext 又會被包裝在 SecurityContextHolder 中。
    1. Authentication 的主要內容
        Authentication 介面通常包含：
        principal：使用者的主要資訊（例如 UserDetails 或自訂的 UserDTO）。
        credentials：憑證（例如密碼，通常在認證後會被清空）。
        authorities：使用者的角色或權限集合。
        details：額外的資訊（例如 IP、session ID）。
        authenticated：布林值，表示是否已認證。

總結 :
    Authentication 是 Spring Security 提供的物件，用來描述目前登入的使用者。
    在 Controller 方法裡可以直接注入 Authentication，不用自己去 SecurityContextHolder 拿。
    透過它，你能取得使用者的基本資訊、角色、權限，並用來做存取控制或資料更新。


    @RequestParam("image") MultipartFile image
            從前端表單或 AJAX 請求中接收上傳的檔案。
            MultipartFile 是 Spring MVC 的標準方式，用來處理檔案上傳。  */
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image) throws InterruptedException {
        UserDTO user = getAuthenticatedUser(authentication);
        userService.updateImage(user, image);
/*
這行程式碼的作用是：在使用者更新大頭貼時，發佈一個 NewUserEvent，事件類型是 PROFILE_PICTURE_UPDATE。
Spring 的事件監聽器會接收並處理這個事件，例如記錄到資料庫或寫入 log。

publisher 是一個 ApplicationEventPublisher，Spring 提供的事件發佈器。
呼叫 publishEvent(...) 之後，Spring 會把事件送到所有有監聽器 (@EventListener) 的地方。
好處是：發佈者和監聽者之間解耦，發佈者不需要知道誰會處理事件。
         */
        publisher.publishEvent(new NewUserEvent(user.getEmail(), PROFILE_PICTURE_UPDATE));
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        // userService.getUserById(user.getId() instead of user, get the fresh data
                        .data(of("user", userService.getUserById(user.getId()), "events", eventService.getEventsByUserId(user.getId()), "roles", roleService.getRoles()))
                        .timeStamp(now().toString())
                        .message("Profile image updated")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }


    //這段程式碼的功能是：讓前端透過 URL 直接取得使用者的個人圖片檔案。
    // content-type: text/html => since the browser tries to decode this as text/html => if we do not specific content type
    // produces image not html => content-type: image/png
    @GetMapping(value = "/image/{fileName}", produces = IMAGE_PNG_VALUE)
    //@GetMapping("/image/{fileName}")
    // {fileName} 是路徑參數，代表要取得的圖片檔案名稱
    // 例如呼叫 /image/alice.png → fileName = "alice.png"。
    public byte[] getProfileImage(@PathVariable("fileName") String fileName) throws Exception {
        // System.getProperty("user.home")：取得使用者主目錄。
        //  => Windows 例子：C:/Users/你的帳號/
        //  =>  macOS/Linux 例子：/home/你的帳號/
        // Files.readAllBytes(...)：讀取整個檔案，並以 byte[] 回傳。方法回傳 byte[]，代表圖片的二進位內容。
        //前端呼叫這個 API 時，會直接拿到圖片檔案的原始資料，通常瀏覽器會自動顯示圖片。
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + fileName));
    }



// refresh token go through shouldNotFilter method and @GetMapping("/refresh/token")
// getSubject => throw exception

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request) {
        if(isHeaderAndTokenValid(request)) {
            String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            /*
            Example 1:
                String myStr = "01234567890";
                System.out.println(myStr.substring(3, 9));
                output : 345678

            Example 2:
            public class Main {
                public static void main(String[] args) {
                    String myStr = "123456";
                    System.out.println(myStr.length()); // 輸出 6
                }
            }


            Example 3:
                public class Main {
                    public static void main(String[] args) {
                        String myStr = "123456";
                        System.out.println(myStr.substring(2));      // 從索引 2 開始 → "3456"
                        System.out.println(myStr.substring(1, 4));   // 從索引 1 到 3 → "234"
                    }
                }

            Example 4:
                public class Main {
                    public static void main(String[] args) {
                        String myStr = "23456";
                        System.out.println(myStr.substring(2));
                        System.out.println(myStr.substring(1, 4));
                    }
                }

            字串內容 :
            myStr = "23456"
            索引位置：
            0 → '2'
            1 → '3'
            2 → '4'
            3 → '5'
            4 → '6'
            第一行
            myStr.substring(2)
            從索引 2 開始到字串結尾。
            索引 2 是 '4' → 結果 "456"
            第二行
            myStr.substring(1, 4)
            從索引 1 開始，到索引 4 之前（不包含 4）。
            索引 1 → '3'，索引 2 → '4'，索引 3 → '5'
            結果 "345"


            這段程式碼的作用是：
            從 HTTP 請求的 Authorization 標頭中取出 Token，並去掉前面的 "Bearer "，只留下真正的 Token 字串。
            說明 :
            (1) private static final String TOKEN_PREFIX = "Bearer ";
                宣告一個常數字串 TOKEN_PREFIX，內容是 "Bearer "。
                這通常用來表示在 HTTP Authorization 標頭中，Token 前面會有 "Bearer " 這個前綴。

        *** String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
            (2) request.getHeader(AUTHORIZATION)
                從 HTTP 請求中取得名為 Authorization 的標頭值。
                例如："Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            (3) .substring(TOKEN_PREFIX.length())
                TOKEN_PREFIX.length() 代表 "Bearer " 的字元長度（7）。
                substring(7) 就是把字串從第 7 個字元開始截取，去掉 "Bearer " 前綴。
                例如："Bearer abc123" → "abc123"
            (4) String token = ...
                把去掉 "Bearer " 前綴後的字串存到變數 token，這就是實際的 Token。
             */
            UserDTO user = userService.getUserById(tokenProvider.getSubject(token, request));
            /*
            說明 :
            (1) tokenProvider.getSubject(token, request)
                tokenProvider 是一個處理 Token 的工具類別。
*****  ***** ** getSubject(...) 方法通常用來從 Token 中解析出 Subject（主體），在 JWT (JSON Web Token) 裡，Subject 一般就是使用者的唯一識別，例如 email 或 userId。
                所以這裡會回傳一個字串，例如 "katie.fan@example.com"。
            (2) userService.getUserByEmail(...)
                userService 是一個服務類別，用來存取使用者資料。
                getUserByEmail(...) 方法會根據 email 去查詢資料庫，並回傳一個 UserDTO 物件，裡面包含使用者的詳細資料（姓名、角色、權限等）。
            (3) UserDTO user = ...
                最後把查詢到的 UserDTO 存到變數 user，方便後續使用。
             */
            return ResponseEntity.ok().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .data(of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user))
                                    , "refresh_token", token))
                            .message("Token refreshed")
                            .status(OK)
                            .statusCode(OK.value())
                            .build());

            /*
http://localhost:8080/user/refresh/token
same refresh_token but new access_token
access_token is expired, we use http://localhost:8080/user/refresh/token to get new access_token
             */
        } else {
            return ResponseEntity.badRequest().body(
                    HttpResponse.builder()
                            .timeStamp(now().toString())
                            .reason("Refresh Token missing or invalid")
                            .developerMassage("Refresh Token missing or invalid")
                            .status(BAD_REQUEST)
                            .statusCode(BAD_REQUEST.value())
                            .build());
        }

    }

    private boolean isHeaderAndTokenValid(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION) != null // if we have an authorization header
                && request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) // if it starts with "bearer "
                && tokenProvider.isTokenValid(
                        tokenProvider.getSubject(request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length()),request),//if valid, take the email
                        request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length())); // and token
    }


    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return ResponseEntity.badRequest().body( //=> shows "400 Bad Request" in Postman
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(BAD_REQUEST)
                        .statusCode(BAD_REQUEST.value())
                        .build());
        /*
        {
            "timeStamp": "2026-01......",
            "statusCode": 400,
            "status": "BAD_REQUEST",
            "reason": "There is no mapping for a POST request for this path on the server"
        }
         */
    }

    /*
    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> handleError(HttpServletRequest request) {
        return new ResponseEntity<>(HttpResponse.builder()
                        .timeStamp(now().toString())
                        .reason("There is no mapping for a " + request.getMethod() + " request for this path on the server")
                        .status(NOT_FOUND)
                        .statusCode(NOT_FOUND.value())
                        .build(), NOT_FOUND);}
     // 建議
     //因為你的錯誤訊息寫的是：
     //There is no mapping for a ... request for this path on the server
     //這其實是「路徑不存在」的情況，所以用 404 Not Found 更符合 REST API 的語意。
     ✅ 一句話總結：
        第一個版本回傳 400（請求錯誤），第二個版本回傳 404（資源不存在）；就語意來說，因為是「沒有對應的路徑」，用 404 比較正確。
     */

/*
這段 authenticate(String email, String password) 方法是一個 使用者登入驗證流程，它結合了 Spring Security 的認證機制 和 Spring 事件系統。
 */
private UserDTO authenticate(String email, String password) {
    UserDTO userByEmail = userService.getUserByEmail(email);
    try {

        // Step 1: 檢查使用者是否存在
        //         userService.getUserByEmail(email)
        //         => 檢查使用者是否存在。若存在，先發佈一個 LOGIN_ATTEMPT 事件。
        if(null != userByEmail) {

            // publisher.publishEvent(new NewUserEvent(...))
            //   → 使用 Spring 的事件機制，發佈一個 NewUserEvent。
            //   → 這些事件會被 NewUserEventListener 監聽，並記錄到資料庫。
            publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT));
        }

        // Step 2: 嘗試進行認證
        //         authenticationManager.authenticate(...)
        //         → 呼叫 Spring Security 的認證邏輯，檢查 email 和 password 是否正確。
        Authentication authentication = authenticationManager.authenticate(unauthenticated(email, password));

        // Step 3: 取得登入後的使用者資訊
        //         getLoggedInUser(authentication)
        //         → 從認證結果裡取得使用者的詳細資訊，封裝成 UserDTO。
        UserDTO loggedInUser = getLoggedInUser(authentication);
        // Step 4: 如果沒有使用 MFA，紀錄成功事件
        if (!loggedInUser.isUsingMfa()) {
            publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_SUCCESS));
        }
        return loggedInUser;
    } catch (Exception exception) {
        if(null != userByEmail) {
            publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_FAILURE));
        }
        // Step 5: 如果失敗，紀錄失敗事件
        // publisher.publishEvent(new NewUserEvent(email, LOGIN_ATTEMPT_FAILURE));
        // Step 6: 處理錯誤（記錄 log、回傳錯誤訊息）
        processError(request, response, exception); //→ 處理錯誤（可能是記錄 log、回傳錯誤訊息）
        // Step 7: 拋出自訂的 ApiException，讓上層知道驗證失敗
        throw new ApiException(exception.getMessage());//→ 拋出自訂的 ApiException，讓上層知道驗證失敗。
    }
}



    // getUri() → 生成新資源的 URI (通常是新建立的使用者路徑)。
    // URI 是 Java 標準類別，用來表示一個統一資源識別符 (Uniform Resource Identifier)，通常就是網址。
    private URI getUri() {
        return URI.create(fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    } /*
        ServletUriComponentsBuilder.fromCurrentContextPath()
           => 這是 Spring 提供的工具類別，用來建立 URI。
           => fromCurrentContextPath() → 取得目前應用程式的基礎路徑。
           => 例如，如果你的系統跑在 http://localhost:8080，這裡就會以 http://localhost:8080 作為起始點。
        */

    //if the user is not using Mfa
    //用來在使用者登入成功後，回傳一個標準化的 HTTP 回應 (ResponseEntity)。
    private ResponseEntity<HttpResponse> sendResponse(UserDTO user) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user, "access_token", tokenProvider.createAccessToken(getUserPrincipal(user))
                        , "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(user))))
                        .message("Login Success")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }



    //📖 取得 UserPrincipal
    // 將 UserDTO 轉換成 UserPrincipal，包含使用者與角色權限。
    //建立一個 UserPrincipal，封裝使用者與權限資訊，供 Spring Security 使用。
    // 用來建立 JWT Token。
    private UserPrincipal getUserPrincipal(UserDTO user) {
        //參數 user 是一個 UserDTO，通常是登入或註冊後的使用者資料。
        // toUser：把 UserDTO 轉換回 User 實體，用於資料庫操作。
        return new UserPrincipal(toUser(userService.getUserByEmail(user.getEmail())), roleService.getRoleByUserId(user.getId())); //pass role only, not role + permission
    //呼叫 userService，透過使用者的 Email 查詢完整的 User 物件。取得使用者的詳細資料（例如密碼、狀態、其他屬性）。
    //呼叫 roleService，透過使用者的 ID 查詢其角色。再從角色物件中取得權限 (Permission)。這些權限會用來控制使用者能執行哪些操作。
    //new UserPrincipal(...) 建立一個新的 UserPrincipal 物件。
    //    => UserPrincipal 通常是 Spring Security 的自訂封裝類別，代表「已驗證的使用者」。
        //   它會包含：
        //     使用者的詳細資料 (User)
        //     使用者的權限 (Permission)
    }


    //📖 MFA 驗證碼回應
    // 名稱是 sendVerificationCode，主要用來發送驗證碼給使用者，並回傳一個標準化的 HTTP 回應。=> Postman
    // ResponseEntity. 在Spring Boot中， ResponseEntity 是一个带有HTTP响应的对象，它封装了响应的状态码、头部信息和响应体。
    private ResponseEntity<HttpResponse> sendVerificationCode(UserDTO user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timeStamp(now().toString())
                        .data(of("user", user)) //give them an access token , and the refresh token as well. and then need to load the user event like login, etc.
                        .message("Verification Code Sent")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }
}












