

import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { NgForm } from "@angular/forms";
import { ActivatedRoute, ParamMap, Router } from "@angular/router";
import { Observable, of, BehaviorSubject, map, startWith, catchError, switchMap } from "rxjs";
import { DataState } from "src/app/enum/datastate.enum";
import { CustomerState, CustomHttpResponse} from "src/app/interface/appstates";
import { State } from "src/app/interface/state";
import { CustomerService } from "src/app/service/customer.service";
import { NotificationService } from "src/app/service/notification.service";

@Component({
  selector: 'app-customer',
  templateUrl: './customer-detail.component.html',
  styleUrls: ['./customer-detail.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})

export class CustomerDetailComponent implements OnInit {
  //customerState$: Observable<State<CustomHttpResponse<CustomerState>>>;

  //你的 customerState$ 宣告的是：
  //也就是「回傳值必須是 Page<Customer> + User」。

  customerState$: Observable<State<CustomHttpResponse<CustomerState>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<CustomerState>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false) //if new BehaviorSubject<boolean>(true) -> show "Loading..."
  isLoading$ = this.isLoadingSubject.asObservable();
  readonly DataState = DataState;
  private readonly CUSTOMER_ID: string = 'id';

  //📌 we need to get user Id from ActivatedRoute
  constructor(private activatedRoute: ActivatedRoute, private customerService: CustomerService, private notificationService: NotificationService) { }


  /*
這段 ngOnInit() 方法是 Angular Component 的初始化邏輯。
它的作用是：當元件建立時，根據路由參數（客戶 ID）呼叫服務取得客戶資料，並用 RxJS 管理不同的 UI 狀態 (LOADING / LOADED / ERROR)。
我來逐步拆解：

1. ngOnInit()
      Angular 生命週期方法，元件初始化時會自動執行一次。
      常用來做 API 呼叫或設定初始狀態。

2. this.activatedRoute.paramMap.pipe(...)
      ActivatedRoute.paramMap 是一個 Observable，會提供目前路由的參數。
      例如路由設定是 /customer/:id，那麼 paramMap.get('id') 就能取到客戶的 ID。

3. switchMap((params: ParamMap) => {...})
      switchMap 用來把路由參數轉換成另一個 Observable。
      這裡是呼叫 customerService.customer$(+params.get('id'))，用 ID 去查詢客戶資料。
      +params.get('id') 把字串轉成數字。

4. customerService.customer$(...)
      這是一個 Service 方法，回傳一個 HTTP Observable，包含客戶資料。

✅ 總結 :
  這段程式碼的作用是：
    從路由取得客戶 ID。
    呼叫 Service 取得客戶資料。
    用 RxJS 管理三種狀態 (LOADING / LOADED / ERROR)。
    UI 可以根據 customerState$ 自動切換顯示 Loading、資料或錯誤訊息。

=================================

你問到：為什麼前端 (Angular) 會知道路由設定是 /customer/:id，而且 ActivatedRoute.paramMap 能取到 id？

1. 路由設定 (app-routing.module.ts)
    在 Angular 裡，你定義了路由表：
      const routes: Routes = [
        { path: 'customers/:id', component: CustomerComponent, canActivate: [AuthenticationGuard] }
      ];
  這行表示：
    當使用者在瀏覽器輸入 /customers/5，Angular Router 會比對路徑。
    :id 是一個 路由參數 (Route Parameter)，代表一個動態值。
    Angular 會把 5 存到 paramMap 裡，並載入 CustomerComponent。

2. Angular Router 的工作
      Angular Router 會監聽瀏覽器的 URL。
      當 URL 符合某個路由規則（例如 /customers/:id），它就會：
      載入對應的 Component (CustomerComponent)。
      把 :id 的值解析出來，放到 ActivatedRoute.paramMap。

3. ActivatedRoute.paramMap
      在 CustomerComponent 裡，你可以注入 ActivatedRoute。
      paramMap 是一個 Observable，會提供目前路由的參數。
      例如：
        this.activatedRoute.paramMap.subscribe(params => {
          console.log(params.get('id')); // 如果 URL 是 /customers/5，這裡會印出 "5"
        });

4. 為什麼前端「知道」？
      因為你在 路由表 (routes) 裡定義了規則：
      /customers/:id → CustomerComponent
      Angular Router 會自動解析 URL，把 :id 的值交給 ActivatedRoute。
      所以前端「知道」是因為你事先在 app-routing.module.ts 裡定義了這個規則。

✅ 總結 :
    你在路由表裡定義了 /customers/:id。
    Angular Router 會解析 URL，把 id 的值放到 ActivatedRoute.paramMap。
    Component 透過 paramMap.get('id') 就能拿到這個值。
  */
  ngOnInit(): void {

    //In app-routing.module.ts => { path: 'customers/:id', component: CustomerComponent, canActivate: [AuthenticationGuard] }, //pass the guard
    //ActivatedRoute.paramMap 是一個 Observable，會提供目前路由的參數。
    //例如路由設定是 /customer/:id，那麼 paramMap.get('id') 就能取到客戶的 ID。
    this.customerState$ = this.activatedRoute.paramMap.pipe(
      switchMap((params: ParamMap) => { //switchMap 用來把路由參數轉換成另一個 Observable。
        return this.customerService.customer$(+params.get(this.CUSTOMER_ID))//用 ID 去查詢客戶資料。
          .pipe(
            map(response => {
              this.notificationService.onDefault(response.message);
              console.log(response);
              this.dataSubject.next(response); //更新 BehaviorSubject，保存最新資料。
              return { dataState: DataState.LOADED, appData: response }; //suppose return profileState$: Observable<State<CustomHttpResponse<Profile>>>; => State (state.ts) : appData?: T; => type of CustomHttpResponse =>
            }),
            startWith({ dataState: DataState.LOADING }),
            catchError((error: string) => {
              this.notificationService.onError(error);
              return of({ dataState: DataState.ERROR, error })
            })
          )
      })
    );
  }

  // in the backend, whatever update the user, don't update the user invoices because it is the new page , keep this invoices
  // but we need to keep the invoices, so the page doesn't break
  updateCustomer(customerForm: NgForm): void {
    this.isLoadingSubject.next(true);
    this.customerState$ = this.customerService.update$(customerForm.value)
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);

          //give me everything you have in the reponse
          this.dataSubject.next({
            ...response,
            data: {
              ...response.data,
              customer: {
                ...response.data.customer,
                invoices: this.dataSubject.value.data.customer.invoices
              }
            }
          });

          this.isLoadingSubject.next(false);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.notificationService.onError(error);
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.ERROR, error })
        })
      )
  }
}
