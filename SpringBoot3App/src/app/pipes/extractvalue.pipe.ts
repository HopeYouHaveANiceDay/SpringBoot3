import { Pipe, PipeTransform } from '@angular/core';
//the second argument 'args' is optional.
@Pipe({
  name: 'ExtractArrayValue'
})
export class ExtractArrayValue implements PipeTransform {

  transform(value: any, args: string): any {
    let total: number = 0;
    if (args === 'number') {
      let numberArray: number[] = [];
      for(let i = 0; i< value; i++) {
        numberArray.push(i);
      }
      return numberArray;
    }
    if (args === 'invoices') {
      value.forEach(invoice => {
        total += invoice.total
      })
      return total.toFixed(2);
    }
    return 0;
  }
}

/*
這段程式碼的用途是建立一個 Angular Pipe，用來在模板中處理不同型態的資料。你在範例中展示了兩種使用情境：計算發票總額 和 建立分頁數字陣列。

1. args = 'number'：
    接收一個數字 value，建立一個陣列 [0,1,2,...,value-1]。
    用於分頁顯示。

   使用情境：分頁顯示
    <li *ngFor="let pageNumber of state?.appData?.data?.page.totalPages | ExtractArrayValue:'number'; let i = index;" class="page-item pointer">
      <a (click)="goToPage(i)" [ngClass]="i == (currentPage$ | async) ? 'active' : ''" class="page-link">
        {{ i + 1 }}
      </a>
    </li>

    state?.appData?.data?.page.totalPages 是一個數字，例如 11。
    Pipe 檢查 args === 'number'，建立 [0,1,2,...,10]。
    *ngFor 迴圈會依序產生分頁按鈕。
    因為陣列從 0 開始，所以在顯示時用 {{ i + 1 }}，讓分頁從 1 開始。
    active class 會標記目前頁面，顯示藍色。

2. args = 'invoices'：
    接收一個發票陣列 value，逐一加總每張發票的 total 欄位。
    回傳加總後的金額（保留兩位小數）。

    使用情境：計算發票總額
      <span class="badge bg-success" style="font-size: 20px;">
        $ {{ state?.appData?.data?.customer.invoices | ExtractArrayValue:'invoices' }}
      </span>

    state?.appData?.data?.customer.invoices 是一個 發票陣列。
    Pipe 會檢查 args === 'invoices'，然後加總所有 invoice.total。
    最後顯示 客戶的總計帳金額。

總結
這個 Pipe 的設計讓你可以：
用 'number' 參數快速生成分頁數字陣列。
用 'invoices' 參數快速計算發票總額。
它的好處是 在模板中直接處理資料，避免在元件中寫重複的邏輯。
*/
