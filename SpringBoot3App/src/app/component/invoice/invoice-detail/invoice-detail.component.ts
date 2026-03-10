
import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';

// ActivatedRoute：用來取得路由參數（例如發票 ID）。
import { ActivatedRoute, ParamMap } from '@angular/router';

// rxjs 的各種工具：
// Observable、BehaviorSubject、switchMap、map、startWith、catchError、of，用來處理非同步資料流。
import { Observable, BehaviorSubject, switchMap, map, startWith, catchError, of } from 'rxjs';

// DataState：列舉型別，表示資料狀態（LOADING、LOADED、ERROR）。
import { DataState } from 'src/app/enum/datastate.enum';

// CustomHttpResponse、Customer、Invoice、User、State：介面定義，描述 API 回傳的資料結構。
import { CustomHttpResponse } from 'src/app/interface/appstates';
import { Customer } from 'src/app/interface/customer';
import { Invoice } from 'src/app/interface/invoice';
import { State } from 'src/app/interface/state';
import { User } from 'src/app/interface/user';

// CustomerService：服務類別，用來呼叫後端 API。
import { CustomerService } from 'src/app/service/customer.service';

// jsPDF：第三方套件，用來產生 PDF。
import { jsPDF as pdf } from 'jspdf';
import { NotificationService } from 'src/app/service/notification.service';

//===========================================================================

//const INVOICE_ID = 'id';


// Component 裝飾器 : 定義這個元件的選擇器、HTML 模板與 CSS 樣式。
@Component({
  selector: 'app-invoice',
  templateUrl: './invoice-detail.component.html',
  styleUrls: ['./invoice-detail.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InvoiceDetailComponent implements OnInit {


  // In customer.service.ts, invoice$ = (invoiceId: number) => <Observable<CustomHttpResponse<Customer & Invoice & User>>>//get the page and user
  // invoiceState$ : 一個 Observable，用來追蹤發票資料的狀態（載入中、成功、錯誤）。
  invoiceState$: Observable<State<CustomHttpResponse<Customer & Invoice & User>>>;

  // dataSubject
  // BehaviorSubject，初始值為 null，用來保存最新的 API 回傳資料。
  private dataSubject = new BehaviorSubject<CustomHttpResponse<Customer & Invoice & User>>(null);

  // isLoadingSubject 與 isLoading$
  // 控制是否顯示「Loading...」。
  // 如果初始值設為 true，畫面會顯示載入中。
  private isLoadingSubject = new BehaviorSubject<boolean>(false) //if new BehaviorSubject<boolean>(true) -> show "Loading..."
  isLoading$ = this.isLoadingSubject.asObservable();

  // DataState : 引用列舉，方便在模板中使用。
  readonly DataState = DataState;

  // INVOICE_ID : 常數字串 'id'，用來從路由參數取得發票 ID。
  private readonly INVOICE_ID: string = 'id';

  //📌 we need to get user Id from ActivatedRoute
  constructor(private activatedRoute: ActivatedRoute, private customerService: CustomerService, private notificationService: NotificationService) { }


  // ngOnInit() 初始化流程
  ngOnInit(): void {

  // 1. 使用 ActivatedRoute.paramMap 取得路由參數。
    this.invoiceState$ = this.activatedRoute.paramMap.pipe(

      // 2. 用 switchMap 把參數轉換成 API 呼叫：
      switchMap((params: ParamMap) => { //switchMap 用來把路由參數轉換成另一個 Observable。

        // 這會呼叫後端 API 取得發票資料。
        // paramMap：回傳一個 ParamMap 物件，裡面存放路由參數。
        // params.get('參數名稱')：用來取得指定的路由參數值。
        // 常見用途：在 ngOnInit() 裡讀取路由參數，然後用這個參數去呼叫後端 API。
        return this.customerService.invoice$(+params.get(this.INVOICE_ID))
          .pipe(
            map(response => {
              this.notificationService.onDefault(response.message);
              console.log(response);
              this.dataSubject.next(response); //更新 BehaviorSubject，保存最新資料。

              // invoice$ give us <Customer & Invoice & User>,
              // and then pass it for "this.invoiceState$"
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

  //這段程式碼的功能是將網頁上的發票內容匯出成 PDF 檔案。
  //這段程式碼的作用是 把網頁上發票的內容轉換成 PDF，並自動以發票號碼命名檔案來儲存。

// 1. exportASPDF(): void { ... }
//      定義了一個名為 exportASPDF 的方法，沒有回傳值。
  exportAsPDF(): void {
    //這行建立 PDF 檔案的檔名。它會從 this.dataSubject.value.data['invoice'] 物件中取得 invoiceNumber（發票號碼），並組合成像是 invoice-12345.pdf 的檔名。
    const filename = `invoice-${this.dataSubject.value.data[`invoice`].invoiceNumber}.pdf`;
    //建立一個新的 PDF 文件物件，變數名稱是 doc。
    const doc = new pdf();

    /*
    doc.html(document.getElementById(`invoice`)
    =>
    <div id="invoice">
                         <div class="invoice-header">
                            <div class="invoice-from">
                               <small>from</small>
                               <address class="m-t-5 m-b-5">
                                  <strong class="text-inverse">HopeYouHaveANiceDay,Inc.</strong><br>
                                  123 Main Steet<br>
                                  Philadelphia, Pennsylvania 15886<br>
                                  Phone: (123) 456-7890<br>
                                  Fax: (123) 456-7890
                               </address>
                            </div>


    使用 doc.html 方法，將網頁上 ID 為 invoice 的 DOM 元素內容轉換成 PDF。
        傳入的設定物件包含：
        margin: 5 → 設定 PDF 的邊界為 5。
        windowWidth: 1000 → 指定渲染時的視窗寬度。
        width: 200 → 設定 PDF 內容的寬度。
        callback: (invoice) => invoice.save(filename) → 當 PDF 生成完成後，執行回呼函式，將 PDF 以前面定義的檔名儲存。*/
    doc.html(document.getElementById(`invoice`), { margin: 5, windowWidth: 1000, width: 200,
      callback: (invoice) => invoice.save(filename) });
  }
}
