
import { ChangeDetectionStrategy, Component, OnInit } from "@angular/core";
import { NgForm } from "@angular/forms";
import { Router } from "@angular/router";
import { Observable, of, BehaviorSubject, map, startWith, catchError } from "rxjs";
import { DataState } from "src/app/enum/datastate.enum";
import { EventType } from "src/app/enum/event-type.enum";
import { Key } from "src/app/enum/key.enum";
import { CustomHttpResponse, Page, Profile } from "src/app/interface/appstates";
import { Customer } from "src/app/interface/customer";
import { State } from "src/app/interface/state";
import { User } from "src/app/interface/user";
import { CustomerService } from "src/app/service/customer.service";
import { NotificationService } from "src/app/service/notification.service";
import { UserService } from "src/app/service/user.service";

@Component({
  selector: 'app-customers',
  templateUrl: './customers.component.html',
  styleUrls: ['./customers.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CustomersComponent implements OnInit {

  customersState$: Observable<State<CustomHttpResponse<Page<Customer> & User>>>;


  private dataSubject = new BehaviorSubject<CustomHttpResponse<Page<Customer> & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false) //if new BehaviorSubject<boolean>(true) -> show "Loading..."

  /* 把 isLoadingSubject 轉成 Observable，暴露給模板使用。
      在 HTML 裡可以寫*/
  isLoading$ = this.isLoadingSubject.asObservable();

  private currentPageSubject = new BehaviorSubject<number>(0);
  currentPage$ = this.currentPageSubject.asObservable();

  private showLogsSubject = new BehaviorSubject<boolean>(false);
  showLogs$ = this.showLogsSubject.asObservable();
  readonly DataState = DataState;

  //📌 建構子 : 注入 UserService，用來呼叫後端 API。
  constructor(private router: Router, private customerService: CustomerService, private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.customersState$ = this.customerService.searchCustomers$()
      .pipe(
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



/*
整體邏輯 :
    使用者提交搜尋表單 → 呼叫 Service → 更新狀態流 customersState$。
    UI 可以透過 async pipe 監聽 customersState$，並用 *ngSwitchCase 顯示不同狀態：
        LOADED → 顯示搜尋結果。
        ERROR → 顯示錯誤訊息。
        （你也可以加 LOADING 狀態，顯示 spinner）。
*/
  searchCustomers(searchForm: NgForm): void {

    //把目前頁數重設為 0。通常用在分頁功能，確保搜尋結果從第一頁開始。
    this.currentPageSubject.next(0);

    //呼叫 customerService.searchCustomers$()，傳入表單裡的 name 欄位。
    //回傳的是一個 Observable，代表非同步的搜尋結果。
    this.customersState$ = this.customerService.searchCustomers$(searchForm.value.name)

      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response};
        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value}),
        catchError((error: string) => {
          this.notificationService.onError(error);
          return of ({ dataState: DataState.ERROR, error })
        })
      )
  }


  goToPage(pageNumber?: number, name?: string): void {
    this.customersState$ = this.customerService.searchCustomers$(name, pageNumber)// (name, pageNumber) ==>> take the parameters from searchCustomers$ = (name:string = '', page: number = 0) => <Observable<CustomHttpResponse<Page & User>>> in customer.service.ts
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

  goToNextOrPreviousPage(direction?: string, name?: string): void {
    this.goToPage(direction === 'forward' ? this.currentPageSubject.value + 1 : this.currentPageSubject.value - 1, name);
  }

  selectCustomer(customer: Customer): void {
    this.router.navigate([`/customers/${customer.id}`])
  }


}
