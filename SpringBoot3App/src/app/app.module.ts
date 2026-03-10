import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { AuthModule } from './component/auth/auth.module';
import { CustomerModule } from './component/customer/customer.module';
import { HomeModule } from './component/home/home.module';
import { InvoiceModule } from './component/invoice/invoice.module';
import { NotificationModule } from './notification.module';




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
  declarations: [
    AppComponent
  ],
  imports: [
    BrowserModule,
    CoreModule, // the HTTP Client Module needs to be moved to the core module.
    AuthModule,
    CustomerModule,
    InvoiceModule,
    HomeModule,
    AppRoutingModule, //catch all url, if the url empty or doesn't match, go to home page // 在 Angular 裡，AppRoutingModule 是一個專門用來管理 應用程式路由 (Routing) 的模組。它的主要用途是把不同的 URL 路徑對應到不同的元件 (Component)，讓使用者在瀏覽器中切換頁面時，Angular 能顯示正確的內容。
    NotificationModule
  ],
  /* 是在 Angular 模組 (例如 AppModule) 裡註冊你的 HTTP 攔截器 (Interceptor)。
📌 拆解說明
1. providers: [...]
      Angular 的 providers 陣列用來註冊服務(Service) 或攔截器。
      在這裡，你把 TokenInterceptor 註冊進去，讓 Angular 知道要使用它。
2. provide: HTTP_INTERCEPTORS
      HTTP_INTERCEPTORS 是 Angular 提供的一個特殊 Injection Token。
      它代表所有的 HTTP 攔截器。
      當你在這裡註冊一個攔截器，Angular 就會把它加入到攔截器鏈 (Interceptor chain)。
3. useClass: TokenInterceptor
      指定要使用的攔截器類別。
      這裡就是你自己寫的 TokenInterceptor。
4. multi: true
      表示可以註冊 多個攔截器。
      如果沒有 multi: true，只會保留最後一個攔截器，其他的會被覆蓋。
      加上 multi: true，Angular 會把所有攔截器組合成一個鏈，依序執行。
  */
  bootstrap: [AppComponent]
})
export class AppModule { }
