import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';

/*
JwtHelperService
    來自 @auth0/angular-jwt 套件。
    提供一些常用的 JWT 工具方法，例如：
    decodeToken(token)：解碼 JWT，取得 payload。
    isTokenExpired(token)：檢查 Token 是否過期。 */
import { JwtHelperService } from '@auth0/angular-jwt';
import { AccountType, CustomHttpResponse, Profile } from '../interface/appstates';
import { User } from '../interface/user';
import { Key } from '../enum/key.enum';

/*
📌 語言
這是 TypeScript 程式碼。
TypeScript 是 JavaScript 的超集，支援型別系統與裝飾器 (decorators)，非常適合用在 Angular 專案。
*/


/*
@Injectable({ providedIn: 'root' }) :
  讓這個 Service 在整個 Angular 應用程式中都是單例 (singleton)，不需要額外在 app.module.ts 註冊。

單例模式 (Singleton Pattern) :
  定義：確保一個類別 (Class) 在應用程式中只存在一個實例 (Instance)。
  作用：提供一個全域存取點，常用於管理共享資源（如資料庫連接、配置設定）。
  目的：避免重複建立物件帶來的系統開銷。
*/
@Injectable()

export class UserService {

/*
  profile$() {
    throw new Error("Method not implemented.");
  }
*/
  private readonly server: string = 'http://localhost:8080';
  private jwtHelper = new JwtHelperService();


/*
HttpClient :
  Angular 內建的 HTTP 客戶端，用來呼叫後端 API。
*/
  constructor(private http: HttpClient) {}

/*
login$ 方法 :
  發送 POST 請求到 ${this.server}/user/login。
  傳送的 body 是 { email, password }。
  回傳型別是 Observable<CustomHttpResponse<Profile>>，符合你定義的 API 回應格式。
  使用 RxJS 的 pipe 搭配 tap(console.log) (除錯用) 和 catchError(this.handleError) (錯誤處理)。
*/
  login$ = (email: string, password: string) => <Observable<CustomHttpResponse<Profile>>>
    this.http.post<CustomHttpResponse<Profile>>
      (`${this.server}/user/login`, { email, password })
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  save$ = (user: User) => <Observable<CustomHttpResponse<Profile>>>
      this.http.post<CustomHttpResponse<Profile>>
        (`${this.server}/user/register`, user)
        .pipe(
          tap(console.log),
          catchError(this.handleError)
        );

  // send the email to reset password
  requestPasswordReset$ = (email: string) => <Observable<CustomHttpResponse<Profile>>>
    this.http.get<CustomHttpResponse<Profile>>
      (`${this.server}/user/resetpassword/${email}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  verifyCode$ = (email: string, code: string) => <Observable<CustomHttpResponse<Profile>>>
    this.http.get<CustomHttpResponse<Profile>>
      (`${this.server}/user/verify/code/${email}/${code}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

      //key : is UUID we generate in the backend
      //type : is for account or pasword reset
  verify$ = (key: string, type: AccountType) => <Observable<CustomHttpResponse<Profile>>>
    this.http.get<CustomHttpResponse<Profile>>
      (`${this.server}/user/verify/${type}/${key}`)//要同 backend 一樣 => @GetMapping("/verify/password/{key}") or  @GetMapping("/verify/account/{key}")
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

/*
為什麼在「更新密碼」的 API 呼叫中，會選擇 PUT 而不是 POST：
🔑 為什麼用 PUT ?
1. PUT 用於更新現有資源
      在 REST 的語意裡，PUT 表示「更新或取代」一個已存在的資源。更新密碼其實就是修改使用者的資料，而不是新增一個新的使用者。
2. POST 用於建立或非冪等操作
      POST 通常用來「建立新資源」或執行一些非冪等（non-idempotent）的操作，例如註冊新帳號。更新密碼並不是建立新帳號，而是修改既有帳號的屬性。  */
  renewPassword$ = (form: { userId: number, password: string, confirmPassword: string }) => <Observable<CustomHttpResponse<Profile>>>
    this.http.put<CustomHttpResponse<Profile>>
      (`${this.server}/user/new/password`, form)//要同 backend 一樣 => @GetMapping("/verify/password/{key}") or  @GetMapping("/verify/account/{key}")
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );


/*
在 TypeScript 或 Angular 中指定回傳型別為 Observable，
需匯入 Observable 並使用泛型標註資料型別（如 Observable<T>）。
這能讓編輯器明確知道資料流內容，並享用 RxJS 操作符。
常用於 HTTP 請求 (HttpClient.get<T>())。

關鍵要點 :
  泛型標註：務必使用 Observable<T> 標註內容型別，避免使用 Observable<any>。
  命名慣例：返回 Observable 的變數或方法通常建議在結尾加上 $ 符號（例如 user$），以識別其為資料流。
  操作符：若需要處理資料，通常會配合 .pipe() 操作符來進行轉換。
  優勢：相較於 Promise，Observable 允許取消請求 (unsubscribe) 且能處裡多個數值。
 */

/*
為什麼很多 Angular / RxJS 程式碼會用 $ 結尾，以及 Observable 是什麼?????

📌 $ 命名慣例
      在 Angular + RxJS 專案裡，變數或方法名稱加上 $，是一種 程式碼慣例，用來提醒開發者：
      這個變數或方法 回傳的是 Observable。
      這樣一看就知道它不是一般的值，而是一個「資料流」。
    例如：
      profile$ = this.userService.profile$();
      這樣你馬上知道 profile$ 是一個 Observable，而不是單純的物件。
👉 $ 本身沒有語法上的特殊意義，它只是 命名規則，幫助程式碼更清晰。

📌 Observable 是什麼
      Observable 是 RxJS 提供的一種型別，用來表示 非同步資料流。
      它的概念是：你可以「訂閱 (subscribe)」這個 Observable，當有新資料或事件發生時，它會通知你。
    例子：HTTP 請求
      this.http.get<User>('http://localhost:8080/user/profile')
        .subscribe({
          next: (user) => console.log(user),
          error: (err) => console.error(err)
        });
      http.get<User>() 回傳的是一個 Observable。
      當後端回應資料時，next 會被觸發。
      如果發生錯誤，error 會被觸發。


📌 為什麼要用 Observable
(1) 非同步處理
      HTTP 請求、WebSocket、事件監聽都可以用 Observable 表示。
(2) 可組合
      你可以用 .pipe(map, filter, catchError...) 來轉換或處理資料流。
(3) Angular 模板整合
    在 HTML 裡可以用 | async 直接訂閱 Observable，不需要手動 .subscribe()。
    範例：
    <div *ngIf="profile$ | async as profile">
      <p>{{ profile.data.name }}</p>
    </div>

    =>  profile$   →   一個 Observable，包著 Profile API 的回應。
        | async    →   Angular 幫你自動訂閱 Observable。
        *ngIf="... as profile"   →  只有有值時才顯示，並把值存成 profile。
        {{ profile.data.name }}  →  顯示 Profile 裡的使用者名稱。

✅ 總結
$ → 命名慣例，表示這個變數/方法回傳的是 Observable。
Observable → RxJS 的核心型別，用來表示非同步資料流，可以被訂閱、轉換、錯誤處理。
在 Angular 專案裡，這種模式非常常見，因為它能讓 API 呼叫、狀態管理、UI 更新都保持一致的「資料流」設計。



*/

/*
📖 詳細解釋
(1) profile$ = () => ...
      宣告一個方法 profile$，命名以 $ 結尾表示它回傳的是 Observable。
      這是一個箭頭函式，呼叫時會執行 HTTP 請求。
(2) <Observable<CustomHttpResponse<Profile>>>
      指定回傳型別是 Observable，裡面包著 CustomHttpResponse<Profile>。
      意思是：這個 API 回應會是一個「自訂的 HTTP 回應物件」，裡面包含 Profile 型別的資料。
(3) this.http.get<CustomHttpResponse<Profile>>(${this.server}/user/profile)
      使用 Angular 的 HttpClient 發送 GET 請求到 http://localhost:8080/user/profile。
      預期後端回傳的 JSON 會符合 CustomHttpResponse<Profile> 的結構。
(4) .pipe(...)
      RxJS 的管線處理，用來在 Observable 資料流中套用運算子。
      tap(console.log)
          側錄 API 回應到 console，不會改變資料流。
          方便除錯，看到後端回傳的內容。
      catchError(this.handleError)
          捕捉錯誤並交給 handleError 方法處理。
          如果 API 呼叫失敗，會回傳一個錯誤 Observable。
*/
  profile$ = () => <Observable<CustomHttpResponse<Profile>>>
    this.http.get<CustomHttpResponse<Profile>>
      (`${this.server}/user/profile`)
      //(`${this.server}/user/profile`, { headers: new HttpHeaders().set('Authorization', 'Bearer eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJHRVRfQVJSQVlTX0xMQyIsImF1ZCI6IkNVU1RPTUVSX01BTkFHRU1FTlRfU0VSVklDRSIsImlhdCI6MTc3MTA4MjU2MSwic3ViIjoiNCIsImF1dGhvcml0aWVzIjpbIlJFQUQ6VVNFUiIsIlJFQUQ6Q1VTVE9NRVIiXSwiZXhwIjoxNzcxMTgyNTYxfQ.4AN-miJp0EPAk79Pry0RYJliuSCVto28sFi6ep2tSmCtxHUX4V3IBokcLqH-wwGddVHE1ucWXYr41pK45flQ4w') }) //=> get error 400 bad request & not found the user...but have user data

      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );


  update$ = (user: User) => <Observable<CustomHttpResponse<Profile>>>
    this.http.patch<CustomHttpResponse<Profile>>

      (`${this.server}/user/update`, user)
      //pass the object => user => The request body: the updated user data you want to send to the backend.

      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

/*
2. 為什麼 refreshToken$ 要加 { headers: {...} } ?

      GET 請求的語法是：http.get(url, options)。
      第二個參數是 options，可以包含 headers、params 等。
      這個 API 要求使用者提供 refresh token 來換取新的 access token。
      因此必須在 Authorization header 裡加上：
          Authorization: Bearer <refresh_token>
      Angular 的 HttpClient.get() 允許你在 options 裡設定 headers，所以這裡傳 { headers: {...} }。
*/
  refreshToken$ = () => <Observable<CustomHttpResponse<Profile>>>
      this.http.get<CustomHttpResponse<Profile>>
        (`${this.server}/user/refresh/token`, { headers: { Authorization: `Bearer ${localStorage.getItem(Key.REFRESH_TOKEN)}`}})

        .pipe(
          tap(response => {
            console.log(response);
            localStorage.removeItem(Key.TOKEN);//access_token
            localStorage.removeItem(Key.REFRESH_TOKEN);
            localStorage.setItem(Key.TOKEN, response.data.access_token);
            localStorage.setItem(Key.REFRESH_TOKEN, response.data.refresh_token);
          }),
          catchError(this.handleError)
        );


        /*
1. map 運算子的角色
      在 RxJS 裡，map(response => { ... }) 的作用是：
      接收上游（這裡是 this.userService.updatePassword$）送來的 response。
      你在 map 裡可以做一些副作用（例如 this.dataSubject.next(...)）。
      最後 必須回傳一個值，這個值會成為下游 Observable 的輸出。
        */
  updatePassword$ = (form: { currentPassword: string, newPassword: string, confirmNewPassword: string }) => <Observable<CustomHttpResponse<Profile>>>
        this.http.patch<CustomHttpResponse<Profile>>
          (`${this.server}/user/update/password`, form)
          .pipe(
            tap(console.log),
            catchError(this.handleError)
          );
/*
你寫的 updateRoles$ 方法就是前端呼叫後端 API 的入口。
它會觸發後端的 @PatchMapping("/update/role/{roleName}") 方法，
後端更新角色並回傳最新資料，前端再用這個回應更新 UI。

=====================================================

1. 為什麼 updateRoles$ 要加 {} ?

      PATCH 請求的語法是：http.patch(url, body, options)。
      第二個參數必須是 request body。
      在這個 API 設計裡，角色名稱已經放在 URL (/user/update/role/${roleName})，所以 body 不需要傳任何資料。
      但是 Angular 的 HttpClient.patch() 一定要有 body 參數，不能省略。
      因此傳一個空物件 {}，表示「沒有額外資料要更新」。

*/
  updateRoles$ = (roleName: string) => <Observable<CustomHttpResponse<Profile>>>
        this.http.patch<CustomHttpResponse<Profile>>
        (`${this.server}/user/update/role/${roleName}`, {}) //傳一個空物件 {}，表示「沒有額外資料要更新」。
        .pipe(
          tap(console.log),
          catchError(this.handleError)
        );


/*
語法解析
  updateAccountSettings$：方法名稱以 $ 結尾，表示它回傳的是一個 Observable。
  參數 settings：一個物件，包含 { enabled: boolean, notLocked: boolean }，這就是表單送出的資料。
  this.http.patch(...)：用 Angular 的 HttpClient 發送 PATCH 請求到後端 /user/update/settings API。
  pipe(...)：用 RxJS 運算子處理回應。
    =>tap(console.log)：把回應印到 console。
    =>catchError(this.handleError)：錯誤處理。
👉 這段程式碼的作用：呼叫後端 API 更新帳號設定，並回傳一個 Observable，裡面包裝後端回應的 JSON。*/
  updateAccountSettings$ = (settings: { enabled: boolean, notLocked: boolean }) => <Observable<CustomHttpResponse<Profile>>>
        this.http.patch<CustomHttpResponse<Profile>>
        (`${this.server}/user/update/settings`, settings)// setting is the body contains { enabled: boolean, notLocked: boolean }.
        // /user/update/settings => same as SpringBoot
        //call the update, pass the information
        .pipe(
          tap(console.log),
          catchError(this.handleError)
        );

  toggleMfa$ = () => <Observable<CustomHttpResponse<Profile>>>
        this.http.patch<CustomHttpResponse<Profile>>
        (`${this.server}/user/togglemfa`, {})
        .pipe(
          tap(console.log),
          catchError(this.handleError)
        );

    updateImage$ = (formData: FormData) => <Observable<CustomHttpResponse<Profile>>>
        this.http.patch<CustomHttpResponse<Profile>>
        (`${this.server}/user/update/image`, formData)
        .pipe(
          tap(console.log),
          catchError(this.handleError)
        );

 logOut() {
    localStorage.removeItem(Key.TOKEN);
    localStorage.removeItem(Key.REFRESH_TOKEN);
  }

/*
1. localStorage.getItem(Key.TOKEN)
      從瀏覽器的 Local Storage 取出你之前存的 JWT。
      Key.TOKEN 應該是一個常數字串，例如 "access_token"。
2. decodeToken<string>(...)
      嘗試解碼 Token，如果能解碼，代表 Token 結構正確。
      如果回傳 null，代表 Token 不存在或格式錯誤。
3. !this.jwtHelper.isTokenExpired(...)
      檢查 Token 是否過期。
      如果沒有過期，回傳 true；過期則回傳 false。
4. 整體邏輯
      如果 Token 能解碼 而且 沒有過期 → isAuthenticated() 回傳 true。
      否則回傳 false。

decodeToken(token)：解碼 JWT，取得 payload。what is payload?
在 JWT（JSON Web Token）裡，payload 指的是 Token 的「內容部分」，也就是存放使用者資訊或聲明 (claims) 的地方。

JWT 的結構 :
一個 JWT 通常分成三段，用 . 分隔：header.payload.signature

1. Header：描述演算法與 Token 類型，例如：
    { "alg": "HS256", "typ": "JWT" }

2. Payload：存放使用者相關的資料（claims），例如：
      {
        "sub": "1234567890",   // 使用者 ID
        "name": "John Doe",    // 使用者名稱
        "role": "admin",       // 使用者角色
        "exp": 1710000000      // 過期時間 (timestamp)
      }
    Payload 的用途
    身份資訊：例如使用者 ID、email、角色。
    授權資訊：例如使用者的權限範圍。
    有效期限：例如 exp (expiration)，用來判斷 Token 是否過期。
3. Signature：用來驗證 Token 是否被竄改。

在你的程式碼裡 :
  this.jwtHelper.decodeToken(localStorage.getItem(Key.TOKEN))
這行就是把 JWT 的 payload 解碼出來，讓你可以讀取裡面的資訊（例如使用者 ID、角色、過期時間）。
然後再用：
/  !this.jwtHelper.isTokenExpired(localStorage.getItem(Key.TOKEN))
   檢查 exp 是否已經過期。
✅ 總結 :
   JWT 的 payload 就是 Token 的「資料部分」，存放使用者資訊和聲明。解碼 payload 可以讓前端知道使用者是誰、有哪些權限，以及 Token 是否還有效。


===========================================================================

JWT 的結構是：
header.payload.signature
1. Header：描述演算法與 Token 類型。
2. Payload：存放使用者資訊與聲明 (claims)。
3. Signature：用來驗證 Token 的真偽。

Signature 是用 Header + Payload 再加上一個秘密金鑰（secret key），透過演算法（例如 HMAC SHA256 或 RSA）計算出來的。
  公式大概是：
    signature = HMACSHA256(
      base64UrlEncode(header) + "." + base64UrlEncode(payload),
      secret
    )
驗證流程 :
1. 當伺服器簽發 JWT 時，它會用自己的 secret key 生成 Signature。
2. 當伺服器收到一個 JWT 時，它會重新計算一次 Signature。
3. 如果計算結果和 Token 裡的 Signature 一致 → Token 沒被竄改。
4. 如果不一致 → Token 的內容（例如 Payload）已被修改，伺服器就會拒絕。

舉例 :
假設有人拿到一個合法的 JWT，然後偷偷把 Payload 裡的 "role": "user" 改成 "role": "admin"：
    Payload 改了 → Signature 就不再正確。
    伺服器驗證時會發現 Signature 不一致 → 判斷這個 Token 是假的。

================================================================================

你想知道「伺服器簽發 JWT」和「伺服器收到 JWT」分別是什麼意思。其實這是 同一個伺服器在不同階段的角色
1. 伺服器簽發 JWT 的時候
      場景：使用者登入系統（例如輸入帳號密碼）。
      動作：伺服器驗證帳號密碼正確後，會用自己的 secret key 生成一個 JWT。
      結果：這個 JWT 包含使用者資訊（payload），並且加上簽章（signature），然後回傳給前端。
      比喻：伺服器就像「發證機構」，簽發一張帶有防偽標章的通行證。

2. 伺服器收到 JWT 的時候
      場景：使用者之後再呼叫需要驗證的 API（例如更新密碼、查詢資料）。
      動作：前端會把 JWT 放在 HTTP Header（通常是 Authorization: Bearer <token>）送給伺服器。
      伺服器驗證：伺服器拿到 JWT 後，會用同樣的 secret key 重新計算簽章，並比對 Token 裡的 signature 是否一致。
      結果：
      如果一致 → Token 沒被竄改，允許請求。
      如果不一致或過期 → 拒絕請求。
      比喻：伺服器就像「檢查員」，收到通行證後會檢查防偽標章，確保是真的。

總結 :
簽發 JWT：伺服器在使用者登入時產生 Token，並加上簽章。
收到 JWT：伺服器在之後的 API 請求裡驗證 Token，確保它沒被竄改且仍有效。
所以「簽發」和「收到」通常是同一個伺服器，只是處於不同階段：
簽發 → 登入階段。
收到 → 使用者存取受保護資源階段。


What we implemented is to only check for the token expiration.
If the refresh token is expired, we require the user to log in again.
   */
  isAuthenticated = (): boolean => (this.jwtHelper.decodeToken<string>(localStorage.getItem(Key.TOKEN)) && !this.jwtHelper.isTokenExpired(localStorage.getItem(Key.TOKEN))) ? true : false;

/*
handleError 方法 :
處理 HTTP 錯誤，並回傳一個 throwError Observable。
throwError(() => errorMessage) 是 RxJS v7+ 的新寫法，必須傳入一個 callback function。
*/
  private handleError(error: HttpErrorResponse): Observable<never> {
    console.log(error);
    let errorMessage: string;
    if (error.error instanceof ErrorEvent) {
      errorMessage = `A client error occurred - ${error.error.message}`;
    } else {
      if (error.error.reason) {
        errorMessage = error.error.reason;
        console.log(errorMessage); // No User found by email: fgrfedfcfdc@gmail.com
      } else {
        errorMessage = `An error occurred - Error status ${error.status}`;
      }
    }
    return throwError(() => errorMessage); //not sure why they made this take a call back function instead.
  }
}


/*
📖 範例流程
LoginComponent 呼叫：

UserService 負責：
  發送 HTTP POST 請求到後端 /user/login。
  處理回應並回傳 Observable。
  統一錯誤處理。
*/
