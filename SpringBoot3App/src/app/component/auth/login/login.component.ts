import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms'; // NgForm → Angular 表單物件，用來取得使用者輸入。
import { Router } from '@angular/router'; // Angular 路由，用來在登入成功後導向其他頁面。
import { BehaviorSubject, catchError, map, Observable, of, startWith } from 'rxjs'; //RxJS → BehaviorSubject, Observable, map, catchError, startWith 等，用來處理非同步登入流程。
import { LoginState } from 'src/app/interface/appstates'; // LoginState → 自訂介面，描述登入狀態。
import { UserService } from 'src/app/service/user.service'; // UserService → 服務類別，封裝呼叫後端 API 的邏輯。
import { DataState } from 'src/app/enum/datastate.enum'; // DataState → Enum，表示資料狀態（LOADING, LOADED, ERROR）。
import { Key } from 'src/app/enum/key.enum'; // Key → Enum，表示 localStorage 的 key 名稱（TOKEN, REFRESH_TOKEN）。
import { NotificationService } from 'src/app/service/notification.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LoginComponent implements OnInit {
  //loginState$ → 一個 Observable<LoginState>，用來追蹤登入狀態。初始值是 LOADED/LOADING。
  loginState$: Observable<LoginState> = of({ dataState: DataState.LOADED });
  //phoneSubject / emailSubject → BehaviorSubject，用來暫存使用者的手機與 email（主要用於 MFA 驗證）。
  private phoneSubject = new BehaviorSubject<string | null>(null);
  private emailSubject = new BehaviorSubject<string | null>(null);
  readonly DataState = DataState;

  //注入 Angular 的 Router 和自訂的 UserService。
  constructor(private router: Router, private userService: UserService, private notificationService: NotificationService){}

  /*
什麼是 ngOnInit ?
    它屬於 Angular Component 的生命週期方法。
    當一個 Component 建立完成、Angular 把它的 依賴注入 (dependency injection) 和 資料綁定 (data binding) 都準備好之後，就會呼叫 ngOnInit()。
    這通常是你在 Component 初始化時放置「啟動邏輯」的地方。

這段程式碼的意思是：
    當這個 Component 初始化時，會立刻檢查使用者是否已登入 (isAuthenticated())。
    如果已登入 → 導向首頁 /。
    如果未登入 → 導向登入頁 /login。
  */
  ngOnInit(): void {
    //if the user is authenticated, we take them to the home page, otherwise, login page
    this.userService.isAuthenticated() ? this.router.navigate(['/']) : this.router.navigate(['/login']);
  }

  //登入方法
  login(loginForm: NgForm): void {

    // 流程解析
    // (1) 呼叫 UserService.login$ → 傳送 email 和 password 到後端 API。
    this.loginState$ = this.userService.login$(loginForm.value.email, loginForm.value.password)


/* (2) map → 處理回應：

    (2.1) 如果使用者啟用 MFA (多因素驗證)：
          存下手機與 email。
          顯示手機號碼最後 4 碼。
          回傳狀態 { LOADED, isUsingMfa: true, loginSuccess: false }。

    (2.2) 如果沒有 MFA：
          把 access_token 和 refresh_token 存到 localStorage。
          導向首頁 /。
          回傳狀態 { LOADED, loginSuccess: true }。
*/
    .pipe(
      map(response => {
        if(response.data.user.usingMfa) { // this usingMfa match the frontend
          this.notificationService.onDefault(response.message);

/*
(1) phoneSubject / emailSubject
    → BehaviorSubject，用來暫存使用者的手機與 email（主要用於 MFA 驗證）。
    private phoneSubject = new BehaviorSubject<string | null>(null);
    private emailSubject = new BehaviorSubject<string | null>(null);

(2) .next(...)
    RxJS Subject 的方法，用來推送新值。
    當呼叫 .next(...) 時，所有訂閱這個 Subject 的地方都會收到更新。

(3) response.data.user.phone / response.data.user.email
    後端 API 回傳的使用者資料。
    這裡把使用者的 手機號碼 和 電子郵件 存到對應的 Subject。

UI 就能根據 isUsingMfa 的邏輯，顯示：
「Verification Code Sent To ...5678」
=> 5678 -> phone no. 12345678
*/
        this.phoneSubject.next(response.data.user.phone);
        this.emailSubject.next(response.data.user.email);
        return { dataState: DataState.LOADED, isUsingMfa: true, loginSuccess: false,
        phone: response.data.user.phone.substring(response.data.user.phone.length - 4) };
      } else {
        this.notificationService.onDefault(response.message);
        localStorage.setItem(Key.TOKEN, response.data.access_token);
        localStorage.setItem(Key.REFRESH_TOKEN, response.data.refresh_token);
        this.router.navigate(['/']);
        return { dataState: DataState.LOADED, loginSuccess: true };
      }
    }),

// (3) startWith → 在 API 回應前，先設定狀態為 { LOADING }。
// loading => making http request to the backend, request some data
// loaded => get a response
// error => get any kind of errors
      startWith({ dataState: DataState.LOADING, isUsingMfa: false }),

// (4) catchError → 如果 API 出錯，回傳 { ERROR, loginSuccess: false }。
      catchError((error: string) => {
        this.notificationService.onError(error);
        return of({ dataState: DataState.ERROR, isUsingMfa: false, loginSuccess: false, error })
      })
    )
  }


/*
(1)方法宣告
    宣告一個方法 verifyCode，參數是 Angular 的 NgForm 表單物件。
    回傳型別是 void，表示這個方法不直接回傳值 */
  verifyCode(verifyCodeForm: NgForm): void {

/*
(2) 呼叫 service
      呼叫 userService 裡的 verifyCode$ 方法，傳入兩個參數：
      this.emailSubject.value → 使用者的 email（通常是從 BehaviorSubject 或 FormControl 取值）。
      verifyCodeForm.value.code → 表單裡輸入的驗證碼。
      verifyCode$ 回傳的是一個 Observable，所以這裡用 this.loginState$ 來接收。
      */
    this.loginState$ = this.userService.verifyCode$(this.emailSubject.value, verifyCodeForm.value.code)

/*
(3) RxJS pipe + map
      使用 RxJS 的 pipe，在資料流中套用 map。
      map 的作用：把 API 回傳的 response 轉換成另一個物件。 */
    .pipe(
        map(response => {
       this.notificationService.onDefault(response.message);
/*
主要流程：
    (a) 存 Token :
      把後端回傳的 access_token 和 refresh_token 存到瀏覽器的 localStorage，方便之後做身份驗證。*/
          localStorage.setItem(Key.TOKEN, response.data.access_token);
          localStorage.setItem(Key.REFRESH_TOKEN, response.data.refresh_token);

//  (b) 驗證成功後，導向首頁 /。
          this.router.navigate(['/']); // go to the Home page

//  (c) 回傳狀態物件 : 回傳一個物件，表示資料已載入 (LOADED)，並且登入成功 (loginSuccess: true)。
          return { dataState: DataState.LOADED, loginSuccess: true };
        }),


// loading => making http request to the backend, request some data
// loaded => get a response
// error => get any kind of errors
        startWith({ dataState: DataState.LOADING, isUsingMfa: true, loginSuccess: false,
          phone: this.phoneSubject.value.substring(this.phoneSubject.value.length -4 )}),// this isUsingMfa is coming from our logic => 'interface LoginState' => isUsingMfa?: boolean; (we defined that)


          catchError((error: string) => {
            this.notificationService.onError(error);
          return of({ dataState: DataState.ERROR, isUsingMfa: true, loginSuccess: false, error, //error message => small error
            phone: this.phoneSubject.value.substring(this.phoneSubject.value.length -4)})
        })
      )
  }

  loginPage(): void {
    this.loginState$ = of({ dataState: DataState.LOADED });
  }

}
