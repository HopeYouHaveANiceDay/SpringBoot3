import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Stats } from 'src/app/interface/stats';

/*
在 Angular 裡，changeDetection: ChangeDetectionStrategy.OnPush 的意思是：這個元件的 變更檢測策略改成「OnPush 模式」。
1. 預設行為（Default）
      Angular 預設使用 ChangeDetectionStrategy.Default。
      這代表 Angular 會在很多情況下自動檢查元件的變化，例如：
        1. 事件觸發（click、input 等）。
        2. 任何資料綁定的變化。
        3. Zone.js 偵測到非同步操作（例如 setTimeout、HTTP 回應）。
      缺點：檢查範圍大，效能消耗較高。

2. OnPush 行為
      當你設定 ChangeDetectionStrategy.OnPush，Angular 只會在特定情況下檢查元件：
        1. @Input() 的值改變（父元件傳進來的資料有新值）。
        2. 元件內觸發事件（例如按鈕 click）。
        3. Observable/Promise 的新值 emit（例如 async pipe）。
      Angular 不會再「全域掃描」所有可能的變化，而是只在這些明確的觸發點更新畫面。

好處 :
  效能提升：減少不必要的檢查，特別是在大型應用程式中。
  更可控：你可以明確決定什麼時候更新 UI。*/

@Component({
  selector: 'app-stats',
  templateUrl: './stats.component.html',
  styleUrls: ['./stats.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush //Angular 唔會 looking for all kinds of changing that can happen. only looking for input change, observable when event is submitted. improve our application performance
})
export class StatsComponent {

  /*
  ✅ 總結
@Input() → 讓子元件能接收父元件傳進來的資料。
stats: Stats → 宣告一個屬性，型別是 Stats，用來存放父元件傳來的統計資料。
整體作用 → 讓 <app-stats> 元件 in "home.component.html" and "stats.component.ts" 可以顯示父元件提供的統計數據。

這一行 @Input() stats: Stats; 的作用就是 讓父元件可以把一個 Stats 物件傳進來。
然後在 stats.component.html 模板裡，你寫的：
    <span>{{ stats?.totalInvoices }}</span>
    Total Invoices
就是在使用這個 stats 物件。
*/
  @Input() stats: Stats;

}
