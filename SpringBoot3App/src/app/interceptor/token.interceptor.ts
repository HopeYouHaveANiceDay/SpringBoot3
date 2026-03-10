import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { BehaviorSubject, catchError, Observable, retry, switchMap, throwError } from 'rxjs';
import { Key } from '../enum/key.enum';
import { UserService } from '../service/user.service';
import { CustomHttpResponse, Profile } from '../interface/appstates';

/*
表示這個攔截器是 Angular 的服務 (Service)，並且在整個應用程式中都可以使用。
✅ 總結
  這是一個 Angular 的 HTTP 攔截器範例。
  目前程式碼只是攔截並直接傳遞請求，沒有修改。
  在實務上，你可以在這裡加上 JWT Token、錯誤處理 或 全域設定。

providedIn: 'root' 代表它會被註冊到全域的依賴注入系統。
  providedIn: 'root'
  =>  表示這個服務會在 整個應用程式的根注入器 (root injector) 中註冊。
      好處是：
        全域可用：你可以在任何地方注入這個服務，而不用在 AppModule 或其他模組的 providers 陣列裡手動註冊。
        單例模式 (Singleton)：整個應用程式只會建立一個實例，所有地方共用。
*/
//@Injectable({ providedIn: 'root' })

@Injectable()

//宣告一個類別 TokenInterceptor，並且實作 Angular 的 HttpInterceptor 介面。
//任何實作 HttpInterceptor 的類別都必須提供一個 intercept 方法。
//攔截器可以攔截所有透過 HttpClient 發出的 HTTP 請求。
export class TokenInterceptor implements HttpInterceptor {

  //if the token is freshing, we do not run the following logic again
  private isTokenRefreshing: boolean = false;

  //BehaviorSubject → 一種 RxJS Subject，會保存最新值並立即提供給新訂閱者。
  //這裡用來管理 刷新 Token 的狀態，並把最新的 CustomHttpResponse<Profile> 推送給所有訂閱者。
  //初始值設為 null，代表一開始還沒有刷新結果。
  private refreshTokenSubject: BehaviorSubject<CustomHttpResponse<Profile>> = new BehaviorSubject(null);


/* constructor(private userService: UserService)
   => 這裡使用 依賴注入 (Dependency Injection)，把 UserService 注入到攔截器裡。
      private 修飾詞表示這個 userService 會成為類別的成員，可以在整個攔截器中使用。
      好處是：你可以在攔截器裡直接呼叫 userService 的方法，例如：
      userService.refreshToken() → 當 Token 過期時自動刷新。
      userService.logout() → 當遇到 403 或其他錯誤時登出。 */
  constructor(private userService: UserService) { } // can use refreshToken$(user.service.ts) here

/*  intercept(...) 方法 :
        攔截所有透過 HttpClient 發出的 HTTP 請求。
        request → 代表即將送出的 HTTP 請求。
        next → 代表下一個攔截器或最終的 HTTP 處理器。*/
  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> | Observable<HttpResponse<unknown>> { // <unknown> = any type of Http Response

    //檢查請求的 URL 是否包含特定字串。
    //這些通常是 不需要 JWT Token 的 API（例如登入、註冊、驗證、刷新 Token、重設密碼）。
    if(request.url.includes('verify') || request.url.includes('login') || request.url.includes('register')
          || request.url.includes('refresh') || request.url.includes('resetpassword')) {

            //如果 URL 符合上述條件，就直接把請求傳遞下去，不做任何修改。
            //也就是說，這些 API 請求不會被攔截器加上 Token。
              return next.handle(request);
      }

      /*
      📌 運作流程 :
          使用者登入 → 後端回傳 JWT Token → 前端儲存在 LocalStorage。
          之後每次呼叫 API：
              攔截器檢查 URL → 如果不是登入/註冊等公開 API，就執行這行程式碼。
              從 LocalStorage 取出 Token。
              呼叫 addAuthorizationTokenHeader 把 Token 加到 Authorization header。
              用 next.handle(...) 把修改後的請求送出。 */
      return next.handle(this.addAuthorizationTokenHeader(request, localStorage.getItem(Key.TOKEN)))
        .pipe(


  /*  錯誤攔截 (catchError)
          攔截回應中的錯誤。
          如果是 401 Unauthorized 且錯誤訊息包含 "expired" → 表示 Token 已過期。
          呼叫 handleRefreshToken(request, next) → 嘗試刷新 Token，再重新執行原本的請求。
          如果是其他錯誤 → 直接拋出，交給全域錯誤處理。    */
          catchError((error: HttpErrorResponse) => { //📌📌📌第一次 401：是因為 Access Token 過期，導致 /user/profile 失敗。
              if(error instanceof HttpErrorResponse && error.status === 401 && error.error.reason.includes('expired')) {
                return this.handleRefreshToken(request, next);
              } else {
                return throwError(() => error); //📌📌📌第二次 500：是因為 Refresh Token 也過期，導致 /user/refresh/token 無法簽發新 Access Token。//the refresh token is expired, we don't do anything, throw exception error 500 internal server error
              }
/*
*** 第一次 401：是因為 Access Token 過期，導致 /user/profile 失敗。
*** 第二次 500：是因為 Refresh Token 也過期，導致 /user/refresh/token 無法簽發新 Access Token。
後端錯誤處理沒有正確回傳 401 Unauthorized，而是回傳了 500 Internal Server Error。

==========================

(1) GET http://localhost:8080/user/profile 401
      呼叫 /user/profile API → 401 Unauthorized
      前端 Angular 嘗試用 Access Token 取得使用者資料。
      因為 Access Token 已過期，後端回傳 401 Unauthorized。

(2) Refreshing Token...
      攔截器偵測到 401 → 嘗試刷新 Token
      攔截器開始執行「Refreshing Token...」。
      呼叫 /user/refresh/token API，這裡使用的是 Refresh Token 來換取新的 Access Token。

(3) GET http://localhost:8080/user/refresh/token 500
      呼叫 /user/refresh/token → 500 Internal Server Error
      後端在驗證 Refresh Token 時失敗。
      錯誤訊息顯示：
        The Token has expired on 2023-03-16T01:45:08Z.
      這裡指的是 Refresh Token 已過期，所以伺服器無法再簽發新的 Access Token。

(4) HttpErrorResponse ... status: 500, statusText: 'OK'
      Angular 捕捉到錯誤回應。
      雖然 statusText 顯示 "OK"，但實際 status 是 500 → 表示伺服器錯誤。
(5) The Token has expired on 2023-03-16T01:45:08Z.
      錯誤訊息顯示 Token 已在指定時間過期。
      因為 Token 過期，TokenProvider.verify(token) 失敗，拋出 TokenExpiredException。

(6) 結果 :
      前端收到 HttpErrorResponse，狀態碼 500。
      因為後端的 TokenProvider.verify(token) 拋出了 TokenExpiredException，但沒有正確轉換成 401 Unauthorized，
      而是被全域錯誤處理器 (@RestControllerAdvice) 當成一般例外 → 回傳了 500 Internal Server Error。*/
          } )
        );
  }

  private handleRefreshToken(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {

    /*  1. 檢查是否正在刷新 Token
          if(!this.isTokenRefreshing) → 如果目前沒有刷新 Token，才開始刷新。
          避免多個請求同時觸發多次 refresh API。 */
    if(!this.isTokenRefreshing) {

      console.log(`Refreshing Token...`);

/* 2. 開始刷新 Token
          this.isTokenRefreshing = true → 標記正在刷新。
          this.refreshTokenSubject.next(null) → 通知訂閱者目前沒有 Token，正在刷新。 */
      this.isTokenRefreshing = true;
      this.refreshTokenSubject.next(null);

/* 3. 呼叫 userService.refreshToken$()
          這是一個 Observable，會呼叫後端的 refresh API。
          成功後回傳新的 Token。 */
      return this.userService.refreshToken$().pipe(

/* 4. switchMap → 使用新 Token 重新送出原本的請求
          this.addAuthorizationTokenHeader(request, response.data.access_token) → 把新 Token 加到原本的請求 header。
          next.handle(...) → 重新送出原本的請求。*/
        switchMap((response) => {
          console.log(`Token Refresh Response`, response);
          this.isTokenRefreshing = false; // 避免重複刷新 //don't run if(!this.isTokenRefreshing) multi

          //next.handle(...) → 重新送出原本的請求。
          this.refreshTokenSubject.next(response);

          console.log('New Token:', response.data.access_token);
          console.log('Sending original request:', request);

          // this.addAuthorizationTokenHeader(request, response.data.access_token)
          // => 把新 Token 加到原本的請求 header。使用新的 Token 重新送出原本的請求
          return next.handle(this.addAuthorizationTokenHeader(request, response.data.access_token))//access_token => come from localStorage (in user.service.ts)
        })
      );



/*  📖 Why the else block is needed????

📖 (1) Scenario 'without' else:
            Imagine multiple API calls happen at the same time.
            The first one detects the token is expired → triggers refreshToken$().
            The second one also detects expired token → but if it calls refreshToken$() again, you’ll end up with multiple refresh requests racing each other.
            This wastes resources and can cause inconsistent state (e.g., overwriting tokens).

📖 (2) Scenario "with" else:
            The first request sets isTokenRefreshing = true and starts the refresh API call.
            Any other requests that come in while refreshing will go into the else block.
            Instead of starting another refresh, they subscribe to refreshTokenSubject.
            When the refresh API finishes, the new token is pushed into refreshTokenSubject.next(response).
            All waiting requests in the else block receive the new token and retry their original request with the updated header.

📌 Flow Example :
        Request A → token expired → enters if → starts refresh API.
        Request B → token expired → enters else → waits on refreshTokenSubject.
        Refresh API returns new token → refreshTokenSubject.next(response) is called.
        Request A retries with new token.
        Request B (and any others waiting) also retries with the same new token.            */
    } else{
      // Same user, multiple requests
      // 如果已經在刷新 Token，等待 refreshTokenSubject 推送新 Token，再送出原本的請求
      this.refreshTokenSubject.pipe(
        switchMap((response) => {
          return next.handle(this.addAuthorizationTokenHeader(request, response.data.access_token))
        })
      )
    }
  }

  private addAuthorizationTokenHeader(request: HttpRequest<unknown>, token: string): HttpRequest<any> {

    //make a copy of the request =>  set the Header => return
    return request.clone({ setHeaders: { Authorization: `Bearer ${token}`}}); //clone => update certain thing. Don't change the data
  }

}
