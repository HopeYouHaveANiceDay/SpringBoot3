import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { NgForm } from "@angular/forms";
import { Observable, of, BehaviorSubject, map, startWith, catchError } from "rxjs";
import { DataState } from "src/app/enum/datastate.enum";
import { EventType } from "src/app/enum/event-type.enum";
import { CustomHttpResponse, Profile } from "src/app/interface/appstates";
import { State } from "src/app/interface/state";
import { NotificationService } from "src/app/service/notification.service";
import { UserService } from "src/app/service/user.service";


@Component({
  selector: 'app-profile',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class UserComponent implements OnInit {
  // profileStates$ => all of the data shows on the profile
  /*
📖 拆解說明
(1) profileState$
      一個 Observable，型別是 State<CustomHttpResponse<Profile>>。
      代表整個 Profile 狀態流，會包含：
          dataState → 狀態 (LOADING, LOADED, ERROR)
          appData → 後端回傳的 Profile 資料
          error → 錯誤訊息（如果有）
      👉 在 HTML 裡用 profileState$ | async 取得最新值。
      👉  | async    →   Angular 幫你自動訂閱 Observable。
      */
  profileState$: Observable<State<CustomHttpResponse<Profile>>>;

  /*
拆解說明 :
1. BehaviorSubject
    RxJS 提供的一種 Subject。
    特點是：它會保存「最新的值」，並且任何新訂閱者都會立即收到這個最新值。
    適合用來管理「狀態」或「資料快照」。

2. 型別 <CustomHttpResponse<Profile>>
      表示這個 BehaviorSubject 存放的資料型別是 CustomHttpResponse<Profile>。
      也就是一個包裝過的 HTTP 回應，裡面包含 Profile 資料。

      例如：
        這段程式碼定義的是一個 ( <T> ) TypeScript 泛型介面 (Generic Interface)，用來描述後端 API 回傳的標準格式。
        這個 CustomhttpResponse<T> 是一個 通用 API 回應格式，用泛型 T 來包裝不同型別的資料，並且提供標準欄位（時間戳記、狀態碼、訊息等），讓前端在處理 API 回應時更一致、更安全。

        export interface CustomHttpResponse<T> {
            timestamp: Date;
            statusCode: number;
            status: string;
            message: string;
            reason?: string; //可選屬性 (?) 代表有些回應可能不需要這些欄位。
            developerMessage?: string;
            data?: T;

3. 初始值 null
      一開始沒有任何資料，所以先放 null。
      之後等 API 回應時，再用 this.dataSubject.next(response) 更新。
  */
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Profile>>(null);

  /*
  代表你建立了一個 BehaviorSubject，型別是 boolean，初始值為 false。
  📖 BehaviorSubject 的特性
      BehaviorSubject 需要一個初始值。
      它會保存「最新的值」，並且在有新訂閱者時立即發送這個值。
      在這裡，初始值是 false → 表示「一開始不是 loading 狀態」。

  📌 為什麼初始值設為 false
        在 Component 初始化時，還沒有呼叫 API → 不需要顯示 loading。
        所以預設 isLoadingSubject 為 false。
        當你開始呼叫 API 時，可以設為 true：
            this.isLoadingSubject.next(true);  // 開始 loading
        當 API 完成或失敗時，再設回 false：
            this.isLoadingSubject.next(false); // 結束 loading

  📌 搭配 isLoading$
      你把它轉成 Observable：
          isLoading$ = this.isLoadingSubject.asObservable();
      這樣在 HTML 裡就能用 | async 直接判斷：
          <button [disabled]="isLoading$ | async">Update</button>
          <span *ngIf="isLoading$ | async">Loading...</span>
   */
  private isLoadingSubject = new BehaviorSubject<boolean>(false) //if new BehaviorSubject<boolean>(true) -> show "Loading..."

  /* 把 isLoadingSubject 轉成 Observable，暴露給模板使用。
      在 HTML 裡可以寫*/
  isLoading$ = this.isLoadingSubject.asObservable();

  private showLogsSubject = new BehaviorSubject<boolean>(false);
  showLogs$ = this.showLogsSubject.asObservable();


  /*
  這個 isLoadingSubject 完全是 前端 Angular 自己的狀態管理，它和 Spring Boot 沒有直接關係。

  📖 為什麼 Spring Boot「不會知道」loading 狀態
            Spring Boot 後端：只負責處理請求，回傳資料或錯誤。它並不會追蹤前端 UI 是否正在 loading。
        Angular 前端：用 BehaviorSubject<boolean> 來表示「目前是否正在載入」。
            初始值 false → 表示一開始不是 loading。
            呼叫 API 前 → this.isLoadingSubject.next(true) → 表示開始 loading。
            API 回應或錯誤後 → this.isLoadingSubject.next(false) → 表示結束 loading。
        👉 所以「loading 狀態」是前端自己決定的，不是 Spring Boot 告訴你的。

  📌 典型流程
      使用者點擊「載入 Profile」按鈕。
      Angular 設定 isLoadingSubject.next(true) → UI 顯示 spinner。
      Angular 呼叫 Spring Boot API /user/profile。
      Spring Boot 回傳資料或錯誤。
      Angular 收到回應 → isLoadingSubject.next(false) → UI 隱藏 spinner。



   */

/*
這樣做的目的主要有兩個：
1. 讓模板 (HTML) 可以直接使用 enum
      在 TypeScript 裡，DataState 和 EventType 通常是 enum 或 常數物件。
      如果你只在 TS 檔案裡 import { DataState } from '...'，那麼在 HTML 模板裡是不能直接用 DataState.SOMETHING 的。
      把它宣告成 component 的 readonly 屬性，就能在模板裡透過 DataState.SOMETHING 或 EventType.SOMETHING 來存取。
2. 保持不可變 (readonly)
      用 readonly 是為了保證這些屬性在 component 裡不會被修改。
      它們只是指向 enum 本身，不需要也不應該被改變。
      這樣可以避免誤操作，讓程式更安全。
總結
  readonly DataState = DataState; 和 readonly EventType = EventType; 的作用是：
  把 enum 暴露給模板使用，讓你在 HTML 裡能直接用 DataState 和 EventType。
  保持不可變，避免在 component 裡被修改。
👉 如果你不加這兩行，模板裡就不能直接用 enum，只能用字串或數字，會失去型別安全。
*/
  readonly DataState = DataState;
  readonly EventType = EventType;

  //📌 建構子 : 注入 UserService，用來呼叫後端 API。
  constructor(private userService: UserService, private notificationService: NotificationService) { }


  /* 在Angular中，当一个组件被创建时，它的生命周期方法 ngOnInit() 会在组件初始化时自动执行一次。
      常用來做「初始化工作」，例如：
          呼叫 API 取得資料。
          訂閱 Observable。
          設定初始狀態。
      這樣可以確保 Component 一開始就有需要的資料或狀態。
  */
  ngOnInit(): void {

    // console.log('Function fired');

    //(1) 呼叫 API : this.userService.profile$() → 呼叫 Service 的 profile$ 方法，回傳一個 Observable。
    this.profileState$ = this.userService.profile$()
      .pipe(


        /*      (2) map(response => {...})
                    當 API 回應成功：
                        console.log(response) → 側錄回應。
                        this.dataSubject.next(response) → 更新 BehaviorSubject 的值。

        ************** 📌 BehaviorSubject 與 next() 📌  **************************
                            dataSubject 是一個 BehaviorSubject，它是一種特殊的 Observable，會保存「最新的值」並且在有新訂閱者時立即發送這個值。
                            next() 是 BehaviorSubject（或一般 Subject）的方法，用來 推送新值到資料流。
                            當你呼叫 this.dataSubject.next(response) 時：
                            response 這個值會被送進 dataSubject。
                            所有訂閱 dataSubject 的地方都會立即收到這個新值。

                        📖 舉例
                            假設你有這樣的程式碼：
                            this.dataSubject.subscribe(value => {
                              console.log("收到新值:", value);
                            });
                            this.dataSubject.next("Hello");
                            this.dataSubject.next("World");

                            輸出結果會是：
                              收到新值: Hello
                              收到新值: World
                            因為每次呼叫 .next()，就會把新值送到所有訂閱者。
        *******************************************************************

                        回傳一個物件：
                        { dataState: DataState.LOADED, appData: response }
                        表示資料已載入，並且把回應存到 appData。  */
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response }; //suppose return profileState$: Observable<State<CustomHttpResponse<Profile>>>; => State (state.ts) : appData?: T; => type of CustomHttpResponse =>
        }),


        /*   (3) startWith(...)
                  在 API 還沒回應之前，先發出一個初始狀態：
                      { dataState: DataState.LOADING, isUsingMfa: false }
                    => 表示正在載入中。   */
        startWith({ dataState: DataState.LOADING }), //can add a skeleton loading animation


        /* (4) catchError(...)
                如果 API 呼叫失敗，回傳一個錯誤狀態：*/
        catchError((error: string) => {
          this.notificationService.onError(error);
          return of({ dataState: DataState.ERROR, appData: this.dataSubject.value, error })
        })
      )
  }



  //這段程式碼的作用是 在 Angular 前端更新使用者的個人資料，並且透過 RxJS 的操作符來管理「載入中狀態」、「成功回應」以及「錯誤處理」。
  updateProfile(profileForm: NgForm): void {

    //一開始呼叫更新時，先把「載入中狀態」設為 true，通常用來顯示 loading spinner。
    this.isLoadingSubject.next(true);

    //呼叫 userService 的 update$ 方法，把表單的值 (profileForm.value) 傳給後端 API。 => Pathch method
    //這會回傳一個 Observable<CustomHttpResponse<Profile>>。
    this.profileState$ = this.userService.update$(profileForm.value) //give us the json object for the key value pair

      //使用 RxJS 的管線操作符來處理回應。
      //RxJS 的 tap 操作符是一个非常有用的工具，它允许我们“查看” Observable 流中的数据，同时不会对数据流产生任何影响。
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);//印出回應內容。
          this.dataSubject.next({ ...response, data: response.data });//更新 dataSubject，保存最新的資料。
          this.isLoadingSubject.next(false); //this.isLoadingSubject.next(false);：把載入狀態改回 false。
          return { dataState: DataState.LOADED, appData: this.dataSubject.value }; //回傳一個物件，表示資料狀態已載入 (DataState.LOADED)，並附上最新的 appData。
        }),

        //startWith(...) 在真正的回應到來之前，先發出一個初始狀態。這樣 UI 可以立即顯示「已載入的舊資料」，避免畫面空白。
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),

        //如果 API 呼叫失敗：把載入狀態改回 false。回傳一個包含錯誤訊息的物件，並保留目前的 appData，讓 UI 不會整個崩潰。
        catchError((error: string) => {
          this.notificationService.onError(error);
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
        })
      )
  }




/*
1. this.dataSubject.next({ ...response, data: response.data });
      作用：這行是把後端回傳的 response 更新到 dataSubject（通常是 BehaviorSubject 或 ReplaySubject）。
      意義：它會觸發所有訂閱 dataSubject 的地方，讓大家拿到最新的資料。
      比喻：就像是把新的資料放進「資料倉庫」，通知所有正在看這個倉庫的人。

2. return { dataState: DataState.LOADED, appData: this.dataSubject.value };
      作用：這行是 map 運算子裡的回傳值，會往下游的 observable（例如 profileState$）送出一個物件。
      意義：它不是更新 dataSubject，而是提供一個「狀態物件」給 UI 使用，裡面包含：
      dataState：目前的狀態（例如 LOADED）。
      appData：目前 dataSubject 裡的值（也就是剛剛更新過的資料）。
      比喻：就像是把倉庫裡的最新快照包裝成一份「報告」，交給前端畫面顯示。

      在這裡「回傳給 UI 的資料模型，讓畫面知道現在的狀態和資料」的意思是：
      什麼是「資料模型」?
        在 Angular 裡，你的 map 運算子最後 return { dataState: DataState.LOADED, appData: this.dataSubject.value }; 這個物件，就是一個 資料模型 (view model)。
        它是一個結構化的物件，裡面包含：
          dataState：目前的狀態（例如 LOADED、LOADING、ERROR），用來讓 UI 判斷要顯示什麼樣的畫面。
          appData：實際的資料內容（來自 dataSubject.value），用來讓 UI 顯示資料。

        為什麼要「回傳給 UI」?
        Angular 的模板（HTML）是透過 profileState$ 這個 observable 來接收資料。
        當 map 回傳 { dataState, appData }，這個物件就會被送到 UI。
        UI 可以根據 dataState 決定顯示「載入中」、「成功」、「錯誤」等不同畫面；同時用 appData 來顯示實際的資料。

        假設你在 HTML 裡這樣寫：
        <div *ngIf="profileState$ | async as state">
          <div *ngIf="state.dataState === DataState.LOADING">載入中...</div>
          <div *ngIf="state.dataState === DataState.LOADED">
            <p>{{ state.appData.data }}</p>
          </div>
          <div *ngIf="state.error">錯誤：{{ state.error }}</div>
        </div>
        這裡的 state 就是你 return 出來的物件。
          state.dataState 告訴 UI 現在是 LOADING 還是 LOADED。
          state.appData 提供實際資料給 UI 顯示。
        ✅ 總結 :
          「回傳給 UI 的資料模型」就是一個物件，裡面包含狀態和資料，讓前端畫面知道：
          現在要顯示什麼狀態（載入中、成功、錯誤）。
          要顯示什麼資料（例如使用者資訊、事件列表）。
          這樣 UI 才能根據不同情況正確渲染畫面。

總結
  next(...) → 更新內部狀態，推送新資料到 subject。
  return {...} → 回傳給 UI 的資料模型，讓畫面知道現在的狀態和資料。
兩者是互補的：
1. 先用 next 更新倉庫（保持狀態一致）。
2. 再用 return 把最新狀態送到 UI（讓畫面顯示正確）。

===============================================================

你看到的這個物件：
{
  timeStamp: '2026-02-....',
  statusCode: 200,
  status: 'OK',
  message: 'Password updated successfully',
  data: {
    events: [
      { id: 30, type: 'PASSWORD_UPDATE', description: 'You updated your password' },
      ...
    ]
  }
}
其實是 後端 API 回傳的 response，而不是 return { dataState: DataState.LOADED, appData: this.dataSubject.value } 本身。
✅ 差異解釋 :
1. 後端回傳的 response
      當你呼叫 this.userService.updatePassword$(...) 時，後端會回傳一個 JSON 物件，通常包含 timeStamp、statusCode、status、message、data。
      你在 map(response => { ... }) 裡面拿到的 response 就是這個物件。
      所以你看到的 {timeStamp, statusCode, status, message, data} 是 後端 API 的原始回應。

2. this.dataSubject.next({ ...response, data: response.data });
      這行是把後端回傳的 response 推送到 dataSubject，更新前端的狀態。
      它會讓 dataSubject.value 變成最新的 response。

3. return { dataState: DataState.LOADED, appData: this.dataSubject.value };
      這行是 map 運算子的回傳值，會送到 profileState$ 這個 observable。
      它不是後端的原始回應，而是你自己包裝的「前端資料模型」，用來讓 UI 知道：
        dataState：目前狀態（LOADED）。
        appData：目前 dataSubject 裡的值（也就是剛剛更新過的 response）。
✅ 總結 :
  你看到的 {timeStamp, statusCode, status, message, data} → 來自後端 API 回應 (response)。
  this.dataSubject.next(...) → 把這個 response 存進前端的 subject。
  return { dataState: ..., appData: ... } → 把 subject 的值包裝成 UI 用的資料模型，送到 profileState$。
所以答案是：這個物件來自後端 API 的回應 (response)，然後再被 next(...) 推送到 subject，最後 UI 透過 return {...} 拿到包裝過的版本。

「後端回應 → dataSubject.next → return view model → UI 顯示」的整個資料流
*/
  updatePassword(passwordForm: NgForm): void {
    this.isLoadingSubject.next(true);
    if (passwordForm.value.newPassword === passwordForm.value.confirmNewPassword) {
      /*
      1. map 運算子的角色
          在 RxJS 裡，map(response => { ... }) 的作用是：
          接收上游（這裡是 this.userService.updatePassword$）送來的 response。
          你在 map 裡可以做一些副作用（例如 this.dataSubject.next(...)）。
          最後 必須回傳一個值，這個值會成為下游 Observable 的輸出。

      這裡的 profileState$ 就是整個管線的 最終 Observable。
      當 map 回傳 { dataState: ..., appData: ... }，這個物件就會被送到 profileState$。
      你的 UI 如果在模板裡 profileState$ | async，拿到的就是這個回傳的物件。
      */
      this.profileState$ = this.userService.updatePassword$(passwordForm.value)
        .pipe(

// 當你呼叫 this.userService.updatePassword$(...) 時，
// 後端會回傳一個 JSON 物件，通常包含 timeStamp、statusCode、status、message、data。
// 你在 map(response => { ... }) 裡面拿到的 response 就是這個物件。
      map(response => {

            this.notificationService.onDefault(response.message);
            console.log(response);

            //這行是把後端回傳的 response 推送到 dataSubject，更新前端的狀態。
            //它會讓 dataSubject.value 變成最新的 response。
            this.dataSubject.next({ ...response, data: response.data });

            passwordForm.reset(); //delete the reset record to show the passord on the webpage if reset password sucessfully
            this.isLoadingSubject.next(false);

        // dataState：目前狀態（LOADED）。
        // appData：目前 dataSubject 裡的值（也就是剛剛更新過的 response）。
            return { dataState: DataState.LOADED, appData: this.dataSubject.value };
          }),
          startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
          catchError((error: string) => {
            this.notificationService.onError(error);
            passwordForm.reset();
            this.isLoadingSubject.next(false);
            return of({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
          })
        )
    } else {
      passwordForm.reset();
      /*
this.notificationService.onDefault(response.message);
    📌 總結（繁體中文）
    錯誤原因：response 在 else 區塊沒有宣告，TypeScript 找不到這個變數。
    解決方法：不要在 else 使用 response.message，改成自訂字串訊息，例如 "Passwords don't match"。
    原則：只有在後端 API 回傳時（map(response => {...})）才能使用 response，在前端驗證錯誤時要自己提供訊息字串。
    👉 換句話說，response 只存在於你有訂閱 API 回應的地方，前端驗證邏輯要自己寫訊息，不要用 response。*/

    // 在 else 區塊裡，你要顯示的是「密碼不一致」的訊息，這是前端驗證錯誤，不需要依賴後端回傳的 response。所以應該改成：
     this.notificationService.onError("Passwords don't match");
      console.log(`passwords don't match`);
      this.isLoadingSubject.next(false);
    }
  }

  updateRole(roleForm: NgForm): void {
    this.isLoadingSubject.next(true);
    //console.log(roleForm);

    //this.profileState$ => 用來存放「使用者角色更新」後的狀態流。
    //this.userService.updateRoles$(...) => 呼叫 userService 物件裡的方法 updateRoles$。參數是 roleForm.value.roleName，也就是表單裡輸入或選擇的角色名稱。
    //roleForm.value.roleName => roleForm 是 Angular 的 NgForm 物件，代表整個表單。.value 會取出表單裡所有欄位的值，形成一個物件。.roleName 表示取出其中名為 roleName 的欄位值（例如 "ROLE_ADMIN"）。
    //這行程式碼的意思是：
    //把 userService.updateRoles$() 回傳的 Observable 指派給 this.profileState$，並且傳入表單裡的角色名稱作為參數
    this.profileState$ = this.userService.updateRoles$(roleForm.value.roleName)
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next({ ...response, data: response.data });
          this.isLoadingSubject.next(false)
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
          startWith ({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
          catchError((error: string) => {
            this.notificationService.onError(error);
            this.isLoadingSubject.next(false);
            return of ({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
        })
      )
  }

  updatePicture(image: File): void {
    if (image) {
      this.isLoadingSubject.next(true);
     this.profileState$ = this.userService.updateImage$(this.getFormData(image))
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);

          //update imageUrl if users change their email
          this.dataSubject.next({ ...response,

/*
(1) 瀏覽器會快取圖片，當你再次請求同一個 URL 時，它可能直接從快取讀取，而不是重新發送請求到伺服器。
    在 Network 面板裡，這種情況會顯示「(from disk cache)」或「(from memory cache)」，而不會顯示完整的時間。
    Chrome Network 沒顯示時間，是因為圖片可能直接從快取讀取。
    如果顯示 (from disk cache) 或 (from memory cache)
    → 表示瀏覽器沒有重新下載，而是直接用快取的檔案。這種情況下「Time」欄位通常是空的或顯示非常小的值，因為根本沒有網路傳輸。

    為什麼你看到沒有時間
    當圖片 URL 沒有改變時，瀏覽器會認為它和之前的檔案一樣，直接用快取。
    所以 Network 面板只顯示 Request URL，但「Size」欄位會標註 (from cache)，而「Time」欄位就不會顯示下載時間。
    Network 面板細節裡，重點是要看 「Size」欄位，因為它能告訴你這個圖片是 真的從伺服器下載，還是 直接從快取讀取

    200 OK (from memory cache)
    表示瀏覽器並沒有真的去伺服器下載圖片，而是直接從 記憶體快取 (memory cache) 取出來。

    Network => Img
    Network => (click) Disable cache


(2) 為什麼第一次上傳後會看到 ?time=1771442811866
    在 updatePicture 方法裡，你有加上：imageUrl: `${response.data.user.imageUrl}?time=${new Date().getTime()}`
    這樣做的目的是避免快取，讓 <img> 標籤重新載入最新圖片。
    所以第一次更新時，Network 面板會顯示帶有 ?time=... 的 URL。

    為什麼刷新頁面後 ?time=... 消失
    當你 重新整理 (refresh) Angular App 時，前端的狀態 (dataSubject 或 profileState$) 會被重置。
    如果後端回傳的 user.imageUrl 是 沒有加上 ?time=... 的原始 URL，那麼前端初始化時就只會顯示：
    http://localhost:8080/user/image/KatieFan@gmail.com.png
    因為 ?time=... 是你在前端動態加上的，並不是後端真正存的 URL。

    為什麼 Network 顯示 200 OK 而不是快取
    你的後端回應 Header 有：cache-control: no-cache, no-store, max-age=0, must-revalidate
    pragma: no-cache
    這代表伺服器明確告訴瀏覽器：不要快取圖片，每次都要重新下載。
    所以即使 URL 沒有 ?time=...，瀏覽器也會重新請求伺服器，Network 面板顯示 200 OK。

總結
  ?time=... 是前端用來強制刷新圖片的技巧，但它只存在於前端狀態，不會存到後端。
  當你刷新頁面，前端狀態重置 → URL 回到原始值 (沒有 ?time=...)。
  不過因為後端已經設定了 no-cache，瀏覽器仍然會重新下載圖片，所以即使沒有 ?time=...，你也能拿到最新圖片。
*/
            data: { ...response.data, //http://localhost:8080/user/image/KatieFan@gmail.com.png?time=1771552811866
              user: { ...response.data.user, imageUrl: `${response.data.user.imageUrl}?time=${new Date().getTime()}`}}});
              //user: { ...response.data.user, imageUrl: `${response.data.user.imageUrl}`}}}); // after uploading the new image, it still the old one because the Url is the same
          this.isLoadingSubject.next(false)
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
          startWith ({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
          catchError((error: string) => {
            this.notificationService.onError(error);
            this.isLoadingSubject.next(false);
            return of ({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
          })
        )
    }
  }







/*
前後端關係 :
    前端 Service (user.service.ts)：定義如何呼叫後端 API。
    前端 Component (profile.component.ts)：在使用者提交表單時，呼叫 Service 方法，並更新前端狀態。
    後端 Spring Boot (@PatchMapping("/update/settings"))：接收 PATCH 請求，更新資料庫，回傳最新的使用者資訊。
流程是：
    使用者在前端修改設定並提交表單。
    Component 呼叫 Service → Service 發送 PATCH 請求到後端。
    後端更新資料庫 → 查詢最新使用者 → 回傳 JSON。
    前端接收 JSON → 更新 profileState$ → 畫面顯示最新狀態。*/

    //這是 Component 裡的方法，當使用者提交表單時會被呼叫。
  updateAccountSettings(settingsForm: NgForm): void {
    //設定狀態為「載入中」。
    this.isLoadingSubject.next(true);
    //this.userService.updateAccountSettings$(settingsForm.value)：
    // => 呼叫 Service 方法，把表單的值（{ enabled, notLocked }）送到後端。
    this.profileState$ = this.userService.updateAccountSettings$(settingsForm.value)
      .pipe(
        //map(response => {...})：處理成功回應，更新 dataSubject，並把狀態改成 LOADED。
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next({ ...response, data: response.data });
          this.isLoadingSubject.next(false);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.notificationService.onError(error);
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.LOADED, appData: this.dataSubject.value, error });

        })
      )
  }

  toggleMfa(): void {
    this.isLoadingSubject.next(true);
    this.profileState$ = this.userService.toggleMfa$()
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next({ ...response, data: response.data });
          this.isLoadingSubject.next(false);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value};
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.notificationService.onError(error);
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.LOADED, appData: this.dataSubject.value, error })
        })
      )
  }

  toggleLogs(): void {
    this.showLogsSubject.next(!this.showLogsSubject.value);
  }

    /*
  ✅ 總結
這個方法的用途是：把圖片檔案包裝成 FormData，以便用 multipart/form-data 格式送到後端。這樣後端的 @RequestParam("image") MultipartFile image 就能正確接收到檔案。 */
  private getFormData(image: File): FormData {
    const formData = new FormData();
    formData.append(`image`, image); // `image` need to match the "Key: image" in Postman
    return formData;
  }
}
/*

1. DataState
    這是一個 型別或列舉 (enum)，通常在 TypeScript 裡會定義成：
        export enum DataState {
          LOADING,
          LOADED,
          ERROR
        }
    它代表一組固定的狀態常數，例如 DataState.LOADED 就是「已載入」的狀態。

2. dataState
      這是一個 物件的屬性名稱，用來存放目前的狀態值。
      在你的程式碼裡，它被用在回傳的物件中：
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
  這裡的 dataState 是物件的 key，而它的值是 DataState.LOADED。

3. 差異總結 :
      DataState：型別或列舉，定義了有哪些可能的狀態（例如 LOADING、LOADED、ERROR）。
      dataState：物件的屬性名稱，用來存放某一個狀態值。
      DataState.LOADED：列舉裡的一個具體值，表示「已載入」。
      dataState: DataState.LOADED：意思是「在這個物件裡，dataState 這個屬性被設定為 DataState.LOADED」。

類比 :
    可以把它想成：
    dataState = 物件裡的欄位，用來存放你選的那個選項
    DataState = 狀態的「字典」或「選單」
    DataState.LOADED = 選單裡的一個選項

 */
