import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpEvent, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { CustomerState, CustomHttpResponse, Page, Profile } from '../interface/appstates';
import { User } from '../interface/user';
import { Stats } from '../interface/stats';
import { Customer } from '../interface/customer';
import { Invoice } from '../interface/invoice';

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

/*
這兩種寫法的差異主要在於 服務的提供方式與生命週期管理：
1. @Injectable({ providedIn: 'root' })
      意思：這個 service 會由 Angular 的 root injector 提供。
      效果：整個應用程式只會建立一個單例 (singleton)，在任何地方注入時都會拿到同一個實例。
      優點：
          1. 不需要在 AppModule 或其他模組的 providers 裡手動註冊。
          2. Angular 會自動進行 tree-shaking：如果這個 service 沒有被使用，就不會被打包進最終 bundle，減少檔案大小。
          3. 適合全域共用的服務，例如認證、API 呼叫、全域狀態管理。

2. @Injectable() // provided by CoreModule
      意思：這個 service 沒有指定 providedIn，所以必須在某個模組（例如 CoreModule）的 providers 陣列裡手動註冊。
      效果：
        只有在該模組被載入時，Angular 才會建立這個 service 的實例。
        如果模組被多次載入（例如 Lazy Loading 模組），可能會產生多個不同的 service 實例。
      用途：
        適合只在特定模組範圍內使用的服務。
        常見做法是把全域服務放在 CoreModule，並且只在 AppModule 匯入一次，避免重複。

✅ 總結
  providedIn: 'root' → 自動註冊，全域單例，支援 tree-shaking。
  @Injectable() → 需要手動在模組註冊，可能產生多個實例，適合模組範圍的服務。
如果你的 service 是全域共用的，建議用 providedIn: 'root'。如果只在某個模組使用，才用 CoreModule 的 providers。


在前面我提到 providedIn: 'root' 的好處，其中一個就是 tree-shaking。
🌳 Tree-shaking 是什麼？
    1. 字面意思：像「搖樹」一樣，把沒有用到的枝葉（程式碼）搖掉。
    2. 在前端打包工具裡：指的是移除那些 沒有被使用的程式碼，讓最終的 bundle 更小、更快。
    3. Angular 的情境：
          如果一個 service 標記為 @Injectable({ providedIn: 'root' })，但在整個應用程式裡完全沒有被注入使用，Angular 在編譯時就會把它排除掉，不會打包進最終的 JavaScript。
          相反地，如果你用 @Injectable() 並在 CoreModule 的 providers 裡註冊，即使這個 service 沒有被使用，它也會被打包進 bundle，因為 Angular 認為它可能會被需要。

✅ 總結
Tree-shaking：移除未使用的程式碼，減少 bundle 大小。
providedIn: 'root' → 支援 tree-shaking，只有用到的 service 才會被打包。
CoreModule providers → 不支援 tree-shaking，所有註冊的 service 都會被打包。
這就是為什麼 Angular 官方建議盡量使用 providedIn 的方式來提供 service。

          */
//@Injectable({ providedIn: 'root' })
@Injectable() //provided by core module
export class CustomerService {

  private readonly server: string = 'http://localhost:8080';

/*
HttpClient :
  Angular 內建的 HTTP 客戶端，用來呼叫後端 API。
*/
  constructor(private http: HttpClient) {}

/*
Object
data: {user: {…}, stats: {…}, page: {…}}
message: "Customers retrieved"
status: "OK"
statusCode: 200
timeStamp: "2026-02... */
  customers$ = (page: number = 0) => <Observable<CustomHttpResponse<Page<Customer> & User & Stats>>>
    this.http.get<CustomHttpResponse<Page<Customer> & User & Stats>>
      (`${this.server}/customer/list?page=${page}`)   // .    ?size=${size}
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

/*
是用來定義一個 函式型的 Observable 工廠，逐步拆解如下 :
1. 宣告方式
      customer$ = (customerId: number) => ...
      這是一個箭頭函式，接收 customerId: number。
      回傳型別被強制指定為 <Observable<CustomHttpResponse<User & Customer>>>。
      意思是：呼叫 customer$(id) 會得到一個 Observable，裡面包著 CustomHttpResponse<User & Customer>。

2. HTTP 呼叫
      this.http.get<CustomHttpResponse<User & Customer>>(`${this.server}/customer/get${customerId}`)
      使用 Angular 的 HttpClient 發送 GET 請求。
      泛型 <CustomHttpResponse<User & Customer>> 表示回傳的 JSON 會符合這個型別。

3. RxJS 運算子
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      )
      tap(console.log)
        → 在資料流中間插入 side-effect，把回傳結果印到 console。
        → 不會改變資料，只是用來除錯。
      catchError(this.handleError)
        → 如果 HTTP 請求失敗，會呼叫 this.handleError。
        → handleError 通常會回傳一個 Observable，避免整個 stream 崩潰。

✅ 總結 :
    這是一個函式，輸入 customerId，回傳一個 HTTP GET 的 Observable。
    成功時會印出 response，失敗時會交給 handleError 處理。
 */
/* <<CustomerState>> is same as <<User & Customer>>

 <<CustomerState>> comes from appstates.ts
    export interface CustomerState {
        user: User;
        customer: Customer;
    } */
  customer$ = (customerId: number) => <Observable<CustomHttpResponse<CustomerState>>>
    this.http.get<CustomHttpResponse<CustomerState>>
      (`${this.server}/customer/get/${customerId}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

      //we update an entire object
  update$ = (customer: Customer) => <Observable<CustomHttpResponse<CustomerState>>>
    this.http.put<CustomHttpResponse<CustomerState>>
      (`${this.server}/customer/update`, customer)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

      // "Customer & User" come from .data(of(...)) in SpringBoot
/*

為什麼 newCustomers$ 要傳入參數 Customer？
    這裡使用 POST 請求。
    POST 的特性是「新增資料」，通常需要在 body 裡傳入要新增的內容。
    API /customer/create 的用途是：建立一個新的客戶。
    因為伺服器需要知道「要建立哪個客戶」，所以必須傳入 Customer 物件（例如 name、email、phone 等）。

如何判斷是否需要傳入參數？
1. 看 HTTP 方法 :
        GET → 主要用來「讀取」資料，通常不需要 body。參數如果有，通常放在 URL query string（例如 /customers?page=1）。
        POST → 用來「新增」資料，通常需要 body。
        PUT / PATCH → 用來「更新」資料，通常需要 body。
        DELETE → 用來「刪除」資料，通常只需要 ID（可能放在 URL）。
2. 看 API 設計 :
        如果 API 是「提供資料」，通常不需要傳入參數。
        如果 API 是「建立或修改資料」，通常需要傳入物件或欄位。

=====================================================

用途：呼叫 API /customer/create，並把一個 Customer 物件送到伺服器，建立新客戶。
回傳型別：CustomHttpResponse<Customer & User>
→ 表示伺服器回傳的 JSON 包含「新建立的客戶」以及「使用者」的資料。
 */
  newCustomers$ = (customer: Customer) => <Observable<CustomHttpResponse<Customer & User>>>
    this.http.post<CustomHttpResponse<Customer & User>>
      (`${this.server}/customer/create`, customer)   // .    ?size=${size}
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

/*
為什麼 newInvoice$ 無傳入參數？
    這裡使用 GET 請求。
    GET 的特性是「讀取資料」，通常不需要在 body 傳入任何東西。
    API /customer/invoice/new 的用途是：伺服器提供建立新發票所需的資料（例如客戶清單、使用者資訊）。
    因為只是「拿資料」，所以不需要傳入參數。

============================================

用途：呼叫 API /customer/invoice/new，通常是要拿到建立新發票所需的資料，例如：
    =>  客戶清單 (Customer[])
    =>  使用者資訊 (User)
回傳型別：CustomHttpResponse<Customer[] & User>
    =>  表示回傳的 JSON 包含「多個客戶」以及「使用者」的資料。
*/
  newInvoice$ = () => <Observable<CustomHttpResponse<Customer[] & User>>>
    this.http.get<CustomHttpResponse<Customer[] & User>>
      (`${this.server}/customer/invoice/new`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

// invoice: Invoice => we defined the infterface Invoice { id: number; invoiceNumber: string:...}
  createInvoice$ = (customerId: number, invoice: Invoice) => <Observable<CustomHttpResponse<Customer[] & User>>>
    this.http.post<CustomHttpResponse<Customer[] & User>>
      (`${this.server}/customer/invoice/addtocustomer/${customerId}`, invoice) //invoice is the request body
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );

  searchCustomers$ = (name: string = '', page: number = 0) => <Observable<CustomHttpResponse<Page<Customer> & User>>>
    this.http.get<CustomHttpResponse<Page<Customer> & User>>
      (`${this.server}/customer/search?name=${name}&page=${page}`)   // .    ?size=${size}
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      );
/*
invoices$ 的功能就是 向後端請求分頁的發票清單，並且回傳一個 Observable，裡面包含：
    發票分頁資料 (Page<Invoice>)
    使用者資訊 (User)
    包在自訂的回應格式 CustomHttpResponse 裡
在 goToPage() 裡使用它，可以讓前端根據頁碼載入不同頁的發票，並更新狀態管理。 */
  invoices$ = (page: number = 0) => <Observable<CustomHttpResponse<Page<Invoice> & User>>>//get the page and user
  this.http.get<CustomHttpResponse<Page<Invoice> & User>> //inside the appstates.ts
      //(`${this.server}/customer/search?name?&page=${page}`)
      (`${this.server}/customer/invoice/list?page=${page}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      )


/* 用來呼叫後端 API 取得指定發票的資料。
1. invoice$ = (invoiceId: number) => ...
      這是一個函式屬性，名稱叫 invoice$。
      接收一個參數 invoiceId: number，代表要查詢的發票 ID。
      回傳型別是 Observable<CustomHttpResponse<Customer & Invoice & User>>，也就是一個 可觀察物件 (Observable)，裡面包著自訂的 HTTP 回應。

2. this.http.get<CustomHttpResponse<Customer & Invoice & User>>(...)
        使用 Angular 的 HttpClient 發送 GET 請求。
        路徑是 ${this.server}/customer/invoice/get/${invoiceId}，會呼叫後端 API /customer/invoice/get/{id}。
        泛型 <CustomHttpResponse<Customer & Invoice & User>> 表示回傳的 JSON 結構會包含：
        Customer → 客戶資料
        Invoice → 發票資料
        User → 使用者資料
        全部包在一個 CustomHttpResponse 物件裡。

3. .pipe(...)
        用 RxJS 的管線操作符來處理回傳的 Observable。
        tap(console.log)
        在資料流中間插入一個 side-effect，把回傳結果印到 console，方便除錯。
        catchError(this.handleError)
        如果 API 呼叫失敗，會進入 catchError，呼叫 this.handleError 來處理錯誤，避免程式崩潰。
*/

// 接收一個參數 invoiceId: number，代表要查詢的發票 ID。
// 呼叫後端 API /customer/invoice/get/{id}，取得指定發票的詳細資料。
// 回傳的資料包含 客戶 (Customer)、發票 (Invoice)、使用者 (User)，並包在 CustomHttpResponse 裡。
// 透過 RxJS 管線可以在 console 印出結果，並在錯誤時進行處理。
  invoice$ = (invoiceId: number) => <Observable<CustomHttpResponse<Customer & Invoice & User>>>//get the page and user
  this.http.get<CustomHttpResponse<Customer & Invoice & User>> //inside the appstates.ts
      //(`${this.server}/customer/search?name?&page=${page}`)
      (`${this.server}/customer/invoice/get/${invoiceId}`)
      .pipe(
        tap(console.log),
        catchError(this.handleError)
      )

// 建立一個方法 downloadReport$ //make the Http request return HttpEvent, it contains blob data
//downloadReport$：這是一個方法，回傳的是 Observable<HttpEvent<Blob>>，代表一個可觀察的資料流，裡面包含 HTTP 請求的事件。
  downloadReport$ = () => <Observable<HttpEvent<Blob>>>//get the page and user
    this.http.get(`${this.server}/customer/download/report`,
     // 設定 HTTP 請求選項 //observe different events
/*
1. reportProgress:
      true：允許追蹤下載進度事件，例如開始、進行中、完成。
2. observe:'events'：
      不是只拿回最終的回應，而是可以監聽整個 HTTP 請求過程中的事件（像是上傳/下載進度）。
3. responseType:
      'blob'：指定回應的資料型態是二進位檔案（Blob），通常用來下載檔案。
      Blob 是用來處理檔案下載的最佳方式，因為它能安全、正確地保存伺服器回傳的二進位資料，讓你在前端可以自由地下載、顯示或進一步處理檔案。*/
      { reportProgress: true, observe: 'events', responseType: 'blob' })
      .pipe(
        tap(console.log), // 在資料流中輸出事件到 console
        catchError(this.handleError)
      )

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
