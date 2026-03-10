package Java.SpringBoot3.utils;

import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import static Java.SpringBoot3.constant.Constants.USER_AGENT_HEADER;
import static Java.SpringBoot3.constant.Constants.X_FORWARDED_FOR_HEADER;
import static nl.basjes.parse.useragent.UserAgent.*;


//Request中获取相关网络信息 · 我们可以编写一个工具类，如`UserAgentUtil.java`，来解析这个字符串。
// 例如，通过查找特定的关键词，我们可以判断是否为移动设备 ..
public class RequestUtils {



// 主要目的是從 HttpServletRequest 取得使用者的 IP 位址 和 裝置資訊 (User-Agent)。

    //2. 取得 IP 位址
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = "Unknown IP"; //預設 IP 為 "Unknown IP"。
        //如果 request 不為空，先嘗試讀取 X-FORWARDED-FOR header（真實客戶端 IP）。
        if (request != null) {
            ipAddress = request.getHeader(X_FORWARDED_FOR_HEADER);
            //如果 header 沒有值，就退回到 request.getRemoteAddr()（直接取得連線來源 IP）。
            if (ipAddress == null || "".equals(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }

//📌 總結
//   這段程式碼的功能就是：
//   讀取 HTTP 請求的 User-Agent → 使用 UserAgentAnalyzer 解析 → 回傳瀏覽器名稱與裝置名稱。
// 3. 取得裝置資訊
// 方法定義: 這是一個靜態方法，名稱為 getDevice，輸入參數是 HttpServletRequest，用來取得瀏覽器或裝置的資訊。
    public static String getDevice(HttpServletRequest request) {

/*  是在建立一個 UserAgentAnalyzer 物件，用來解析 HTTP 請求中的 User-Agent 字串。
    1. UserAgentAnalyzer.newBuilder()
            建立一個 建構器 (Builder)，準備配置並建立 UserAgentAnalyzer。
            UserAgentAnalyzer 是 YAUAA (Yet Another UserAgent Analyzer) 提供的工具，用來分析瀏覽器送來的 User-Agent header。
    2. .hideMatcherLoadStats()
            隱藏載入時的統計資訊。
            預設情況下，YAUAA 會在初始化時顯示它載入了多少規則、匹配器等。這個方法可以避免在 log 裡出現這些訊息。
    3. .withCache(1000)
            設定快取大小為 1000。
            意思是：當解析 User-Agent 字串時，會把結果暫存在快取裡。如果同樣的字串再次出現，就直接從快取取出結果，而不用重新解析。
            好處：提升效能，特別是在高併發的 Web 環境下。
    4. .build()
            最後呼叫 build()，真正建立一個 UserAgentAnalyzer 實例。
            這個物件就能用來解析 User-Agent header。  */
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer.newBuilder().hideMatcherLoadStats().withCache(1000).build();

/*  使用 YAUAA (Yet Another UserAgent Analyzer)：
    這是一個專門解析 User-Agent 字串的工具。
    parse(request.getHeader(USER_AGENT_HEADER)) → 解析瀏覽器送來的 User-Agent header。   */
        UserAgent agent = userAgentAnalyzer.parse(request.getHeader(USER_AGENT_HEADER));

        //System.out.println(agent);

        // try.yauaa.basjes.nl
        // Field=> Agent: Name => Chrome

        //return agent.getValue(OPERATING_SYSTEM_NAME) + " - " + agent.getValue(AGENT_NAME) + " - " + agent.getValue(DEVICE_NAME);

/* 輸出資訊：
        AGENT_NAME → 瀏覽器或應用程式名稱 (例如 Chrome, Safari)。
        DEVICE_NAME → 裝置名稱 (例如 iPhone, Desktop)。   */
        return agent.getValue(AGENT_NAME) + " - " + agent.getValue(DEVICE_NAME);
    }
}
