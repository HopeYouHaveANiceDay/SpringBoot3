import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationGuard } from 'src/app/guard/authentication.guard';
import { HomeComponent } from './home/home.component';


const homeRoutes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthenticationGuard] },
];


@NgModule({
    imports: [RouterModule.forChild(homeRoutes)], // forRoot vs forChild // forChild is part of the child route //(app.module.ts) 在 Angular 裡，AppRoutingModule 是一個專門用來管理 應用程式路由 (Routing) 的模組。它的主要用途是把不同的 URL 路徑對應到不同的元件 (Component)，讓使用者在瀏覽器中切換頁面時，Angular 能顯示正確的內容。
    exports: [RouterModule]
})
export class HomeRoutingModule { }

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
