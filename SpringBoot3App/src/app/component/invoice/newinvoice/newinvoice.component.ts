import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Observable, BehaviorSubject, map, startWith, catchError, of } from 'rxjs';
import { DataState } from 'src/app/enum/datastate.enum';
import { CustomHttpResponse, Page } from 'src/app/interface/appstates';
import { Customer } from 'src/app/interface/customer';
import { State } from 'src/app/interface/state';
import { User } from 'src/app/interface/user';
import { CustomerService } from 'src/app/service/customer.service';
import { NotificationService } from 'src/app/service/notification.service';

@Component({
  selector: 'app-newinvoice',
  templateUrl: './newinvoice.component.html',
  styleUrls: ['./newinvoice.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NewinvoiceComponent implements OnInit {
  newInvoiceState$: Observable<State<CustomHttpResponse<Customer[] & User>>>;
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Customer[] & User>>(null);
  private isLoadingSubject = new BehaviorSubject<boolean>(false);
  isLoading$ = this.isLoadingSubject.asObservable();
  readonly DataState = DataState;

  constructor(private customerService: CustomerService, private notificationService: NotificationService) { }

  ngOnInit(): void {
    this.newInvoiceState$ = this.customerService.newInvoice$()
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: response };
        }),
        startWith({ dataState: DataState.LOADING }),
        catchError((error: string) => {
          this.notificationService.onError(error);
          return of({ dataState: DataState.ERROR, error })
        })
      )
  }

/**
1. BehaviorSubject 的角色
BehaviorSubject 是 RxJS 的一種 Subject，可以保存「最新的值」並讓所有訂閱者拿到。
你這裡的型別是 CustomHttpResponse<Customer[] & User>，也就是說 dataSubject 預期保存的是一個 包含 customers 清單和 user 資料的回應物件。

2. 你在 newInvoice() 裡的更新方式
this.dataSubject.next(response);
當你呼叫 createInvoice$ 時，後端回傳的 response 可能只包含 新建立的 Invoice 和一些訊息。
它並不包含完整的 customers 清單。
你直接用 next(response) 覆蓋掉 dataSubject，結果就是原本的 customers 清單被替換掉了。

3. 模板依賴 customers
<option *ngFor="let customer of state?.appData?.data?.customers" [ngValue]="customer.id">
  {{ customer.name }}
</option>
這裡的 customers 來源就是 dataSubject.value.data.customers。
但因為你覆蓋掉了 dataSubject，裡面不再有 customers，第二次打開表單時 Customer 下拉選單就失效。

4. 為什麼其他欄位正常
Services、Date、Total、Status 都是單純的表單欄位，不依賴外部清單。
所以即使 dataSubject 被覆蓋，它們仍然能正常顯示和使用。
Customer 欄位依賴 customers 清單，清單消失就失效。


✅ 解決方式
有兩種：
✅ 方法 A：獨立管理 Customers 清單
      在 ngOnInit() 時就載入 customers，存在一個獨立的 Observable，不要依賴 dataSubject。
      customers$: Observable<Customer[]> = this.customerService.getCustomers$();

      模板改成：
        <option *ngFor="let customer of customers$ | async" [ngValue]="customer.id">
          {{ customer.name }}
        </option>

✅ 方法 B：更新狀態時保留 Customers
        在 map() 裡更新 dataSubject 時，不要直接覆蓋掉整個 state，而是保留原本的 customers：

        this.dataSubject.next({
          ...this.dataSubject.value,
          message: response.message,
          data: {
            ...this.dataSubject.value.data,
            invoice: response.data.invoice
          }
        });

✅ 總結 :
      你的 dataSubject 初始是用來保存 customers 清單 + user。
      但在建立發票後，你用 next(response) 覆蓋掉了整個狀態，導致 customers 清單消失。
      所以 Customer 下拉選單失效是 前端狀態管理問題，不是後端 bug。
      解法是：獨立管理 customers 清單，或 更新狀態時保留 customers。

=========================================================================

之所以能判斷「你在 newInvoice() 裡用 this.dataSubject.next(response) 之後，customers 清單會消失」，
是因為 前端狀態管理的型別設計和後端 API 的回傳內容之間存在落差。

1. 你的 dataSubject 型別 :
private dataSubject = new BehaviorSubject<CustomHttpResponse<Customer[] & User>>(null);
這表示 dataSubject 一開始是用來保存 客戶清單 (Customer[]) + 使用者 (User) 的回應。
所以在第一次載入頁面時，state.appData.data.customers 是有值的，Customer 下拉選單能正常顯示。

2. 你在 newInvoice() 裡的更新方式 :
this.dataSubject.next(response);
當你呼叫 createInvoice$ 時，後端回傳的 response 是 新建立的 Invoice。
它並不是 Customer[] & User，而是另一種型別（例如 Invoice）。
你直接用 next(response) 覆蓋掉 dataSubject，結果就是原本的 customers 清單被替換掉了。

3. 模板依賴 customers :
<option *ngFor="let customer of state?.appData?.data?.customers" [ngValue]="customer.id">
  {{ customer.name }}
</option>
這裡的 customers 來源就是 dataSubject.value.data.customers。
但因為 dataSubject 被覆蓋成只有 invoice 的 response，裡面沒有 customers，所以第二次打開表單時 Customer 下拉選單失效。

✅ 總結
你的 dataSubject 初始保存的是 customers 清單 + user。
在建立發票後，你用 next(response) 覆蓋掉了整個狀態。
後端回傳的 response 沒有 customers 清單，所以前端把它丟掉了。
Customer 下拉選單失效是 前端狀態管理問題，不是後端 bug。

👉 建議：
獨立管理 customers 清單（方法 A），或
更新狀態時保留 customers（方法 B）。


4. 如何確認
要完全確定，必須看後端 Spring Boot Controller 的程式碼，
原來係因為SpringBoot CustomerResource.java 錯左！！！ 冇return 到 customers !!!

你問「為什麼要完全確定，必須看後端 Spring Boot Controller 的程式碼？」——原因在於 前端只能推斷 API 的語意，但真正的回傳內容是由後端決定的。
1. 前端只能宣告型別
      在 Angular 裡你寫了：
      this.http.post<CustomHttpResponse<Customer[] & User>>(...)
      這只是 TypeScript 的型別宣告，告訴編譯器「我期望後端回傳這個型別」。
      但 TypeScript 不會檢查後端實際回傳的 JSON 結構。
      如果後端回傳的不是 Customer[] & User，前端仍然會照單全收，只是資料結構不符合預期。

2. 後端才是真正的資料來源
      Spring Boot Controller 決定了 API 的回傳內容

3. 為什麼需要看後端程式碼
      推斷不足：前端只能猜測 API 的設計，但不能保證。
      確定結構：只有看後端 Controller 的回傳型別，才能確定 response 裡到底有沒有 customers。
      避免型別錯誤：如果前端宣告和後端回傳不一致，就會出現像你現在的問題：Customer 下拉選單失效。
 */
  newInvoice(newInvoiceForm: NgForm): void {
    this.dataSubject.next({ ...this.dataSubject.value, message: null });
    this.isLoadingSubject.next(true);
    this.newInvoiceState$ = this.customerService.createInvoice$(newInvoiceForm.value.customerId, newInvoiceForm.value)
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response);
          newInvoiceForm.reset({ status: 'PENDING' });
          this.isLoadingSubject.next(false);
          this.dataSubject.next(response);
          return { dataState: DataState.LOADED, appData: this.dataSubject.value };

        }),
        startWith({ dataState: DataState.LOADED, appData: this.dataSubject.value }),
        catchError((error: string) => {
          this.notificationService.onError(error);
          this.isLoadingSubject.next(false);
          return of({ dataState: DataState.LOADED, error })
        })
      )
  }

}
