import { NgModule } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import { CacheInterceptor } from '../interceptor/cache.interceptor';
import { TokenInterceptor } from '../interceptor/token.interceptor';
import { UserService } from '../service/user.service';
import { CustomerService } from '../service/customer.service';
import { HttpCacheService } from '../service/http.cache.service';
import { NotificationService } from '../service/notification.service';


/*
是 Angular 裡 AppModule 的一部分。它的作用是告訴 Angular 這個模組有哪些 元件 (components) 需要被宣告與管理。
📌 拆解說明
(1) @NgModule 裝飾器
    Angular 用來定義一個模組 (Module)。
    模組是 Angular 應用程式的基本組織單位，負責把元件、指令、管道、服務等組合在一起。
(2) declarations 陣列
    用來宣告這個模組裡有哪些 元件 (components)、指令 (directives)、管道 (pipes)。
    只有被宣告的元件，才能在這個模組的範圍內使用。
(3) 你宣告的元件
    AppComponent → 主應用程式元件，通常是根元件。
    LoginComponent → 登入頁面。
    RegisterComponent → 註冊頁面。
    VerifyComponent → 驗證碼頁面 (MFA 驗證)。
    ResetpasswordComponent → 重設密碼頁面。
*/

/*
這是一個典型的 核心模組 (Core Module) 用來集中管理全域服務與攔截器。
📌 程式碼解析
1. UserService, CustomerService, HttpCacheService
    => 這些是全域服務，透過 providers 註冊後，整個應用程式都能注入同一個實例 (singleton)。


2. HTTP_INTERCEPTORS
    Angular 提供的特殊 injection token，用來註冊 HTTP 攔截器。
      useClass: TokenInterceptor → 在每個 HTTP 請求加上 Token（例如 JWT）。
      useClass: CacheInterceptor → 處理快取邏輯，避免重複請求。
      multi: true → 表示可以有多個攔截器，會依照註冊順序依次執行。

⚠️ 注意事項
CoreModule 只在 AppModule 匯入一次
CoreModule 通常只在 AppModule 匯入，避免重複載入，否則可能會產生多個 service 實例。


為什麼在 CoreModule 裡用 providers，就不需要在 Service 上加 providedIn: 'root'？

1. providedIn: 'root' 的作用
      當你在 Service 上寫：
          Injectable({ providedIn: 'root' })
      Angular 會自動把這個 Service 登記到 root injector，整個應用程式就能使用它，而且是單例。
      好處是：不用手動在 AppModule 或 CoreModule 的 providers 裡加，並且支援 tree-shaking。

2. 在 CoreModule 的 providers 裡註冊
      當你在 CoreModule 裡寫：
          providers: [UserService, CustomerService, HttpCacheService]
      Angular 會把這些 Service 登記到 CoreModule 的 injector。
      如果 CoreModule 只在 AppModule 匯入一次，那麼這些 Service 也會是全域單例。
      因此，不需要再在 Service 裡加 providedIn: 'root'，因為它已經透過 providers 被註冊了。

======================================================================================

在 Angular 專案裡建立 CoreModule 是一種常見的架構最佳實務，原因主要有以下幾點：

📌 為什麼需要 CoreModule
1. 集中管理全域服務
把像 UserService、CustomerService、HttpCacheService 這些全域單例服務放在 CoreModule，避免分散在各個 Feature Module。
這樣能確保服務只會被建立一次，並且容易維護。

2. 註冊全域攔截器
像 TokenInterceptor、CacheInterceptor 必須透過 providers 註冊，CoreModule 是最合適的地方。
這樣所有 HTTP 請求都會自動套用攔截器，不需要在每個模組重複設定。

3. 避免重複載入
CoreModule 通常只在 AppModule 匯入一次。
這樣可以防止服務被多次建立，確保全域單例的特性。

4. 清晰的架構分層
Angular 專案常見分層：
  1. AppModule：應用程式入口，匯入 CoreModule 與 SharedModule。
  2. CoreModule：放全域服務、攔截器、單例物件。
  3. SharedModule：放共用元件、指令、管道（可在多個 Feature Module 重複使用）。
  4. Feature Modules：各功能模組，專注於特定業務邏輯。
這樣的分層能讓專案結構更清晰，維護更容易。


注意事項
CoreModule 只在 AppModule 匯入一次，不要在其他 Feature Module 匯入，否則會產生多個 service 實例。
一般服務如果用 @Injectable({ providedIn: 'root' }) 就不需要放在 CoreModule，但攔截器必須放在 CoreModule 的 providers。

✅ 總結
建立 CoreModule 的目的就是：
集中管理全域服務與攔截器
確保單例服務只被建立一次
提供清晰的專案架構分層

👉 簡單來說：
CoreModule 是專案的「核心大腦」，負責全域性的東西；
SharedModule 是「工具箱」，負責共用的元件/管道；
Feature Module 則是「專業部門」，各自處理不同功能。
*/


/**
 在 Angular 裡，為什麼需要在 CoreModule 匯入 HttpClientModule？原因是這樣的：

📌 1. 啟用 Angular 的 HTTP 功能
HttpClientModule 是 Angular 提供的模組，裡面包含了 HttpClient 服務。
如果沒有匯入它，Angular 根本不會知道要如何處理 HTTP 請求。
例如你的 UserService、CustomerService、HttpCacheService 很可能都會用到 HttpClient 去呼叫後端 API，沒有這個模組就會報錯。

📌 2. 攔截器需要它
你在 CoreModule 裡註冊了：
{ provide: HTTP_INTERCEPTORS, useClas: TokenInterceptor, multi: true },
{ provide: HTTP_INTERCEPTORS, useClass: CacheInterceptor, multi: true }
這些攔截器是專門用來攔截 HttpClient 發出的請求。
如果沒有匯入 HttpClientModule，攔截器就不會生效，因為 Angular 沒有建立 HttpClient 的 injector。

📌 3. CoreModule 是全域入口
把 HttpClientModule 放在 CoreModule，可以確保整個應用程式只有一份 HttpClient，並且所有服務與攔截器都能共用。
這樣避免在每個 Feature Module 重複匯入，保持結構乾淨。

✅ 總結
HttpClientModule 必須匯入一次，通常放在 CoreModule 或 AppModule。
它提供 HttpClient 服務，讓你能呼叫 API。
它也是攔截器運作的基礎，沒有它攔截器就不會生效。
👉 簡單來說：匯入 HttpClientModule 就是打開 Angular 的 HTTP 功能開關，讓你的服務和攔截器能正常工作。

===================================================================================

HttpClient 是 Angular 提供的一個服務，用來在前端應用程式中透過 HTTP 協定與後端 API 溝通。它能讓你發送 GET、POST、PUT、DELETE 等請求，並且支援型別安全、攔截器、錯誤處理與非同步操作。

📌 什麼是 HttpClient
      來源：HttpClient 來自 Angular 的 @angular/common/http 模組。
      用途：讓前端程式能夠呼叫 RESTful API 或其他 HTTP 服務，下載或上傳資料。
      特點：
      型別安全：可以指定回傳資料的型別，避免手動轉換。
      攔截器支援：可搭配 HTTP_INTERCEPTORS 在請求或回應前後加入邏輯（例如加上 JWT Token、快取）。
      錯誤處理：提供一致的錯誤處理機制。
      非同步操作：回傳 Observable，方便與 RxJS 搭配使用。

📌 為什麼重要
      沒有匯入 HttpClientModule → 無法使用 HttpClient，也無法讓攔截器生效。
      有了 HttpClient → 能夠安全、方便地與後端 API 溝通，並且支援 Angular 的依賴注入與 RxJS。

✅ 總結 :
      HttpClient 是 Angular 的 HTTP API，用來呼叫後端服務。
      必須在 AppModule 或 CoreModule 匯入 HttpClientModule 才能使用。
      它支援型別安全、攔截器、錯誤處理與 RxJS 非同步操作，是 Angular 與後端溝通的核心工具。
 */
@NgModule({
  imports: [ HttpClientModule ],
  providers: [
    UserService, CustomerService, HttpCacheService, NotificationService,
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true },
               { provide: HTTP_INTERCEPTORS, useClass: CacheInterceptor, multi: true }
              ],
  /* 是在 Angular 模組 (例如 AppModule) 裡註冊你的 HTTP 攔截器 (Interceptor)。
📌 拆解說明
1. providers: [...]
      Angular 的 providers 陣列用來註冊服務 (Service) 或攔截器。
      在這裡，你把 TokenInterceptor 註冊進去，讓 Angular 知道要使用它。
2. provide: HTTP_INTERCEPTORS
      HTTP_INTERCEPTORS 是 Angular 提供的一個特殊 Injection Token。
      它代表所有的 HTTP 攔截器。
      當你在這裡註冊一個攔截器，Angular 就會把它加入到攔截器鏈 (Interceptor chain)。
3. useClass: TokenInterceptor
      指定要使用的攔截器類別。
      這裡就是你自己寫的 TokenInterceptor。
4. multi: true
      表示可以註冊 多個攔截器。
      如果沒有 multi: true，只會保留最後一個攔截器，其他的會被覆蓋。
      加上 multi: true，Angular 會把所有攔截器組合成一個鏈，依序執行。
  */

})
export class CoreModule { }
