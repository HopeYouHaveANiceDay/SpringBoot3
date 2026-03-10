package Java.SpringBoot3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

//http://localhost:8080/user/refresh/token
//same refresh_token but new access_token
//access_token is expired, we use http://localhost:8080/user/refresh/token to get new access_token

@SpringBootApplication //(exclude = { SecurityAutoConfiguration.class })//ignore SecurityAutoConfiguration
public class SpringBoot3Application {
	private static final int STRENGHT = 12;


	public static void main(String[] args) {
		SpringApplication.run(SpringBoot3Application.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() { //because this is a method so we need to do ()

		return new BCryptPasswordEncoder(STRENGHT);
	}

	// used for a backend application. I can check a request coming from 4200 with the specific header.
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
		CorsConfiguration corsConfiguration = new CorsConfiguration(); //建立一個新的 CorsConfiguration 物件，準備設定跨域規則。
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
         */
		corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000", "http://SpringBoot3.org")); //設定允許的來源（Origin）。只有來自這些網址的請求才允許跨域存取。常見用法是允許前端開發環境（Angular 在 4200 port、React 在 3000 port）以及正式網站。
		//corsConfiguration.serAllowedOrigins(Arrays.asList("*"));//註解掉的程式碼，原本是允許所有來源（*）。這樣會比較寬鬆，但不安全，通常不建議在正式環境使用。


		//設定允許的 HTTP Header。
		// 例如 Authorization（JWT Token）、Content-Type、Accept 等。
		// 這些 header 在跨域請求時會被允許。
		// 瀏覽器發現前端 (4200) 嘗試跨來源呼叫後端 (8080)，if 後端回應中沒有 Access-Control-Allow-Origin 標頭, then 被阻擋。
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
		urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
		return new CorsFilter(urlBasedCorsConfigurationSource);
	}
}
/* https://medium.com/@heather_programming/spring-security-相關術語-e822b79207b2
Authentication 是一個介面，由 AbstractAuthenticationToken 實作，而根據不同的驗證方法，可以用不同的子類別繼承這個抽象類。
而在帳號密碼驗證裡，我們採用 UsernamePasswordAuthenticationToken。

📖 什麼是 AbstractAuthenticationToken
	類別位置：org.springframework.security.authentication.AbstractAuthenticationToken
	性質：它是一個 抽象類別 (abstract class)，繼承自 Authentication 介面的基礎實作。
	用途：提供了大部分 Authentication 物件的共通邏輯，例如：
		管理使用者的 GrantedAuthority（角色/權限）。
		管理 details（請求的額外資訊，例如 IP、Session）。
		管理 authenticated 狀態（是否已驗證）。
		覆寫了 toString() → 輸出完整的 Principal、Credentials、Authorities 等。
換句話說，它是一個「模板類別」，幫助其他具體的 AuthenticationToken 類別少寫很多重複程式碼。

📖 為什麼 UsernamePasswordAuthenticationToken 繼承了 AbstractAuthenticationToken
UsernamePasswordAuthenticationToken 是 Spring Security 提供的一個具體實作，用來表示「使用者透過帳號密碼登入」的憑證。
它需要：
	保存 principal（通常是使用者的 email 或 UserDetails）。
	保存 credentials（通常是密碼，驗證後會設為 [PROTECTED]）。
	保存 authorities（使用者的角色/權限）。
	這些功能大部分已經在 AbstractAuthenticationToken 寫好，所以它只要繼承就能直接使用。

👉 這樣設計的好處是：
	減少重複程式碼：不用每個 Token 類別都自己管理 authorities、details、authenticated 狀態。
	一致性：所有 Token 類別（例如 UsernamePasswordAuthenticationToken、RememberMeAuthenticationToken、JwtAuthenticationToken）都遵循相同的結構。
	可擴充性：你可以自訂新的 Token 類別，只要繼承 AbstractAuthenticationToken，就能快速整合進 Spring Security。

📖 總結
AbstractAuthenticationToken → Spring Security 的抽象基底類別，提供共通的 Authentication 邏輯（權限、細節、驗證狀態、toString）。
UsernamePasswordAuthenticationToken → 繼承它，專門用來表示「帳號密碼登入」的憑證。
原因 → 透過繼承，避免重複程式碼，確保所有 Token 類別有一致的行為。

✅ 一句話總結：
AbstractAuthenticationToken 是 Spring Security 的基底抽象類別，封裝了共通的驗證邏輯；UsernamePasswordAuthenticationToken 繼承它，是專門用來處理帳號密碼登入的具體實作。
 */
/*
📌 相同点 :
		主版本号一致：2.7.2 和 2.7.5 都是 Spring Boot 2.7.x 系列，API 和功能框架基本相同。
		兼容性：两者都基于 Java EE (javax)，而不是 Spring Boot 3.x 的 Jakarta EE (jakarta)。
 */