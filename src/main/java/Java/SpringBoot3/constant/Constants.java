package Java.SpringBoot3.constant;

/*
Constant Class => 是在類中定義的固定、不可變的值，一旦初始化就無法更改。
    不變性：一旦定義，它們的價值在程式的生命週期中保持不變。
    可見性：預設情況下，它們通常是公開的，這使得它們非常適合定義固定的共享配置資料。
    命名慣例：雖然區分大小寫，但最佳做法是使用所有大寫字母（例如，MAX_SIZE）。
    初始化：常量在宣告時必須初始化。

按語言劃分的示例：
    Java：用於定義固定的、不變的資料，通常在final或enum。
    PHP：class MyClass { const CONSTANT = 'value'; }。
    C#：class Calendar { public const int Months = 12; }。
    TypeScript：使用建構函式中的readonly屬性實現。
 */
public class Constants {


    //OR private static final String[] PUBLIC_URLS = { "/user/register/**" };
    /*
    PUBLIC_URLS = { "/user/login/**", "/user/verify/code/**" }
        這裡的 /** 是 Ant-style 路徑匹配 的語法。
        表示匹配該路徑下的所有子路徑。
        例如：
            /user/login
            /user/login/abc
            /user/login/extra/path
            都會被匹配到。
        → 適合用在 Spring Security 配置（例如 http.authorizeRequests().antMatchers(PUBLIC_URLS).permitAll()），因為它支援通配符。

    為什麼需要兩種 :
    PUBLIC_URLS → 用在 Spring Security 的配置，PUBLIC_URLS → 安全框架層級，用 Ant-style 通配符，能一次放行整個路徑群組。
    PUBLIC_ROUTES → 用在自訂 Filter 的邏輯，通常只比對精確路徑，避免過度放行。
     */

    // all thing associate with security
    public static final String[] PUBLIC_URLS = { "/user/verify/password/**", "/user/login/**", "/user/verify/code/**", "/user/register/**", "/user/resetpassword/**", "/user/verify/account/**",
            "/user/refresh/token/**", "/user/image/**", "/user/new/password/**" };
//private static final String[] PUBLIC_URLS = { "/**" };


    public static final String TOKEN_PREFIX = "Bearer ";

    /* 宣告一個字串陣列 PUBLIC_ROUTES，目前只放了一個空字串。
       這個陣列用來定義「公開路徑」，也就是不需要授權驗證的 API 路徑。
       之後可以在這裡加入像 /login、/register 等路徑。

    PUBLIC_ROUTES = { "/user/login", "/user/verify/code" }
    這裡沒有 /**，只是一個精確的字串。
    表示只匹配完全相同的 URI。
    例如：
        /user/login ✅
        /user/login/abc ❌（不會匹配）
    → 適合用在 Filter 判斷（例如 request.getRequestURI()），因為這裡是字串比對，不支援 Ant-style 通配符。

    為什麼需要兩種 :
    PUBLIC_URLS → 用在 Spring Security 的配置，支援通配符，能一次放行整個路徑群組。
    PUBLIC_ROUTES → 用在自訂 Filter 的邏輯，通常只比對精確路徑，避免過度放行。
     */
    //so we can use the same public url without /** -> defining new one
    public static final String[] PUBLIC_ROUTES = {"/user/new/password", "/user/login", "/user/verify/code", "/user/register", "/user/refresh/token", "/user/image"};
    /*
    1️⃣ CustomAuthorizationFilter 的 PUBLIC_ROUTES:
    這是你自訂的 Filter 判斷邏輯，用來決定是否跳過 Token 驗證。
它只影響 Filter 是否執行，但不會改變 Spring Security 的整體授權規則。
換句話說：Filter 可以放行，但如果 SecurityConfig 沒有允許匿名訪問，請求還是會被 Security 攔截。
     */
    public static final String HTTP_OPTIONS_METHOD = "OPTIONS";

    public static final String AUTHORITIES = "authorities"; //"authorities": ["READ:USER","READ:CUSTOMER"] //用來在 JWT 中存放使用者的 權限/角色資訊 (Claims)。
    public static final String GET_ARRAYS_LLC = "Hope_You_Have_A_Nice_Day"; //通常用來設定 JWT 的 簽發者 (Issuer)，代表這個 Token 是由哪個系統或公司簽發。
    public static final String CUSTOMER_MANAGEMENT_SERVICE = "CUSTOMER_MANAGEMENT_SERVICE"; //通常用來設定 JWT 的 受眾 (Audience)，表示這個 Token 是給哪個服務或系統使用。
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 432_000_000;
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 432_000_000; //長整數常數 432,000,000 毫秒。代表 存取權杖 (Access Token) 的有效期限。換算時間：432,000,000 ms ÷ 1000 ÷ 60 ÷ 60 ÷ 24 ≈ 5 天。意思是 Token 在簽發後 5 天內有效，之後就會過期。
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";


    // Request
    //USER_AGENT_HEADER → 瀏覽器或應用程式會在 HTTP 請求裡帶上 User-Agent header，描述使用者的裝置、瀏覽器、作業系統等資訊。
    public static final String USER_AGENT_HEADER = "user-agent";


    //X-FORWARDED-FOR_HEADER → 反向代理或 Load Balancer 常用的 header，用來記錄真實的客戶端 IP。
    public static final String X_FORWARDED_FOR_HEADER = "X-FORWARDED-FOR";

    // Date
    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
}

