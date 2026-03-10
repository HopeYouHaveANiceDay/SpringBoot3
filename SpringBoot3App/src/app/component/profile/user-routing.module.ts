import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationGuard } from 'src/app/guard/authentication.guard';
import { UserComponent } from './user/user.component';


/*
第一種：用 children 巢狀結構，通常搭配 Lazy Loading 或模組化路由。
    路徑：外層 path: ''，內層也是 path: ''。
    效果：這樣的設定表示當路由是 /（根路徑）時，就會載入 UserComponent。
    用途：常見於模組化路由（例如 UserModule），用 children 來定義子路由。這樣可以在 AppRoutingModule 裡用 loadChildren 載入，保持結構清晰。*/
const userRoutes: Routes = [
  {
    path: '',
    children: [
      { path: '', component: UserComponent, canActivate: [AuthenticationGuard] }
    ]
  }



];
/*
這兩段路由設定的差異主要在於 路徑 (path) 與 巢狀結構 (children) 的使用方式：

第二種寫法 :
  { path: 'profile', component: UserComponent, canActivate: [AuthenticationGuard] }
    路徑：直接指定 path: 'profile'。
    效果：當路由是 /profile 時，才會載入 UserComponent。
    用途：比較直覺，適合單一路由，不需要巢狀結構。

如果你要在 UserModule 裡定義路由，通常會用第一種；
如果只是單純在 AppRoutingModule 裡定義 /profile 頁面，就用第二種。
*/


@NgModule({
    imports: [RouterModule.forChild(userRoutes)], // forRoot vs forChild // forChild is part of the child route //(app.module.ts) 在 Angular 裡，AppRoutingModule 是一個專門用來管理 應用程式路由 (Routing) 的模組。它的主要用途是把不同的 URL 路徑對應到不同的元件 (Component)，讓使用者在瀏覽器中切換頁面時，Angular 能顯示正確的內容。
    exports: [RouterModule]
})
export class UserRoutingModule { }

/*
In angular.json, 注意：這裡用的是 bootstrap.min.js，它需要 Popper.js 才能正常運作。但如果改用 bootstrap.bundle.min.js，就已經包含 Popper，不需要額外安裝。

Popper.js 是一個專門用來處理 浮動元素定位 的 JavaScript 函式庫。

📌 核心概念
什麼是 Popper?
在網頁中，任何「跳出來」的 UI 元件都可以叫做 popper，例如：
    Tooltip (提示文字)
    Popover (彈出訊息框)
    Dropdown (下拉選單)
這些元件需要精確定位在某個元素旁邊，並且要避免被螢幕邊界裁切。

🔧 Popper.js 的作用 :
    自動計算 位置 (positioning)，讓浮動元素顯示在正確位置。
    處理 邊界溢出 (overflow) 問題，避免 tooltip 或 dropdown 跑到螢幕外。
    提供 彈性 API，可以搭配 Bootstrap、Material UI 等框架使用。
*/
