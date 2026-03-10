
import { HttpEvent, HttpEventType } from "@angular/common/http";
import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";

import { Router } from "@angular/router";
import { Observable, of, BehaviorSubject, map, startWith, catchError } from "rxjs";
import { DataState } from "src/app/enum/datastate.enum";

import { CustomHttpResponse, Page, Profile } from "src/app/interface/appstates";
import { Customer } from "src/app/interface/customer";
import { State } from "src/app/interface/state";
import { Stats } from "src/app/interface/stats";
import { User } from "src/app/interface/user";
import { CustomerService } from "src/app/service/customer.service";
import { saveAs } from 'file-saver';
import { NotificationService } from "src/app/service/notification.service";

/*
在 Angular 裡，@Component({...}) 是 告訴框架這個類別是一個 Component 的必要裝飾器。
沒有它，Angular 就只會把你的類別當成普通的 TypeScript 類別，而不是能被渲染的 UI 元件。

為什麼需要 @Component？
1. 宣告這是一個 Component
    Angular 透過 @Component 裝飾器來識別哪些類別是 UI 元件。
    如果沒有這個裝飾器，Angular 編譯器就會報錯：
    "The class 'HomeComponent' is listed in the declarations of the NgModule 'AppModule', but is not a directive, a component, or a pipe."

2. selector
    定義這個 Component 在 HTML 裡的標籤名稱。
    例如 selector: 'app-home' → 你可以在其他模板裡用 <app-home></app-home> 來插入這個元件。

3. templateUrl
    指定這個 Component 的 HTML 模板檔案。
    Angular 會把這個 HTML 渲染成 UI。
    如果檔案名稱錯誤（例如你寫成 profile.component.html 但檔案實際是 home.component.html），就會出現「找不到模板檔案」的錯誤。

4. styleUrls
    指定這個 Component 的 CSS 樣式檔案。
    讓這個元件的樣式獨立管理。
*/
@Component({
  selector: 'app-home', // ✅ 改成 app-home
  templateUrl: './home.component.html',// ✅ 指向 home.component.html
  styleUrls: ['./home.component.css'], // ✅ 指向 home.component.css
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class HomeComponent implements OnInit {
  // profileStates$ => all of the data shows on the profile
  /*
📖 拆解說明
(1) homeState$
      一個 Observable，型別是 State<CustomHttpResponse<Profile>>。
      代表整個 Profile 狀態流，會包含：
          dataState → 狀態 (LOADING, LOADED, ERROR)
          appData → 後端回傳的 Profile 資料
          error → 錯誤訊息（如果有）
      👉 在 HTML 裡用 profileState$ | async 取得最新值。
      👉  | async    →   Angular 幫你自動訂閱 Observable。
      */
  homeState$: Observable<State<CustomHttpResponse<Page<Customer> & User & Stats>>>;

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
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Page<Customer> & User & Stats>>(null);

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

  private currentPageSubject = new BehaviorSubject<number>(0);
  currentPage$ = this.currentPageSubject.asObservable();

  private showLogsSubject = new BehaviorSubject<boolean>(false);
  showLogs$ = this.showLogsSubject.asObservable();


  // percent is used to present the progress
  private fileStatusSubject = new BehaviorSubject<{ status: string, type: string, percent: number }>(undefined);
  fileStatus$ = this.fileStatusSubject.asObservable();

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

  //📌 建構子 : 注入 UserService，用來呼叫後端 API。
  constructor(private router: Router, private customerService: CustomerService, private notificationService: NotificationService) { }


  /* 在Angular中，当一个组件被创建时，它的生命周期方法 ngOnInit() 会在组件初始化时自动执行一次。
      常用來做「初始化工作」，例如：
          呼叫 API 取得資料。
          訂閱 Observable。
          設定初始狀態。
      這樣可以確保 Component 一開始就有需要的資料或狀態。
  */
  ngOnInit(): void {

    // console.log('Function fired');

    //(1) 呼叫 API : this.customerService.profile$() → 呼叫 Service 的 customers$ 方法，回傳一個 Observable。
    this.homeState$ = this.customerService.customers$()

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
          return of({ dataState: DataState.ERROR, error })
        })
      )
  }


  goToPage(pageNumber?: number): void {
    this.homeState$ = this.customerService.customers$(pageNumber)
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next(response);
          this.currentPageSubject.next(pageNumber);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.notificationService.onError(error);
          return of({ dataState: DataState.LOADED, error, appData: this.dataSubject.value })
        })
      )
  }

  goToNextOrPreviousPage(direction?: string): void {
    this.goToPage(direction === 'forward' ? this.currentPageSubject.value + 1 : this.currentPageSubject.value - 1);
  }

  selectCustomer(customer: Customer): void {
    this.router.navigate([`/customers/${customer.id}`])
  }

  report(): void {
    this.homeState$ = this.customerService.downloadReport$()
      .pipe(
        map(response => {
          console.log(response);
          this.reportProgress(response);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value}),
        catchError((error: string) => {
          return of({ dataState: DataState.LOADED, error, appData: this.dataSubject.value })
        })
      )
  }

  //用來處理 HTTP 請求過程中的不同事件，例如上傳/下載進度、回應標頭、完整回應等，方便在前端顯示進度或處理檔案。
  private reportProgress(httpEvent: HttpEvent<string [] | Blob>): void {

    //根據事件的型態來做不同的處理。
    switch(httpEvent.type) {

      // 報告下載或上傳進度
      // 表示正在下載或上傳資料，可以在這裡計算進度百分比並顯示給使用者。
      case HttpEventType.DownloadProgress:
        this.fileStatusSubject.next({ status: 'progress', type: 'Downloading', percent: Math.round(100 * httpEvent.loaded / httpEvent.total)});
        break;

      // 收到回應標頭
      // 表示伺服器已經回傳了回應標頭（headers），但內容還沒完全到達。
      case HttpEventType.ResponseHeader:
        console.log('Got response Headers', httpEvent);
        break;

      // 收到完整回應（例如檔案或資料）
      // 表示完整的回應已經收到，例如檔案下載完成或 API 回傳的資料。
      case HttpEventType.Response:




      //這是 file-saver 套件提供的方法，用來在瀏覽器中觸發檔案下載。
      saveAs(

        //new File([...], fileName, options)
        //建立一個新的 File 物件，內容來自伺服器回傳的 Blob。
        new File(

          //把 HTTP 回應的主體（body）轉成 Blob，作為檔案的內容。
          [<Blob>httpEvent.body],

          //從回應標頭中取得檔案名稱，作為下載時的檔名。
          httpEvent.headers.get('File-Name'),

/*       { type: ... }：指定檔案的 MIME 類型（例如 application/pdf、
          application/vnd.ms-excel），並加上字元編碼 charset=utf-8。  */
          { type: `${httpEvent.headers.get('Content-Type')};charset=utf-8` }
        )
      )

        break;

      // default : 如果事件型態不是以上幾種，就把事件輸出到 console 以便除錯。
      default:
        console.log(httpEvent);
        break;
    }
  }


}
