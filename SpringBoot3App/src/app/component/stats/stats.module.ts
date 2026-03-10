import { ChangeDetectionStrategy, NgModule } from '@angular/core';
import { SharedModule } from 'src/app/shared/shared.module';
import { StatsComponent } from './stats.component';


/*
是 Angular 裡 AppModule 的一部分。它的作用是告訴 Angular 這個模組有哪些 元件 (components) 需要被宣告與管理。
📌 拆解說明
(1) @NgModule 裝飾器
    Angular 用來定義一個模組 (Module)。
    模組是 Angular 應用程式的基本組織單位，負責把元件、指令、管道、服務等組合在一起。
(2) declarations 陣列
    用來宣告這個模組裡有哪些 元件 (components)、指令 (directives)、管道 (pipes)。
    只有被宣告的元件，才能在這個模組的範圍內使用。
(3) 你宣告的元件
    AppComponent → 主應用程式元件，通常是根元件。
    LoginComponent → 登入頁面。
    RegisterComponent → 註冊頁面。
    VerifyComponent → 驗證碼頁面 (MFA 驗證)。
    ResetpasswordComponent → 重設密碼頁面。
*/
@NgModule({
  declarations: [ StatsComponent ],
  imports: [ SharedModule ],
  exports: [ StatsComponent ],
})
export class StatsModule { }
