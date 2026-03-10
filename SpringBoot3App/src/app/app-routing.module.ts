import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './component/home/home/home.component';
import { AuthenticationGuard } from './guard/authentication.guard';
/*
(1) 當使用者進入 /user/verify/account/某個key，會顯示 VerifyComponent。
    :key 是路由參數，可以用來傳遞驗證 token 或唯一識別碼。

(2) 當使用者進入 /user/verify/password/某個key，同樣會顯示 VerifyComponent。
    雖然使用同一個元件，但路由不同，元件內可以依據 URL 判斷是「帳號驗證」還是「密碼驗證」。

(3) { path: '**', component: LoginComponent }
    這是一個 catch-all (通配路由)。
    當使用者輸入不存在的路由時，會自動導向到 LoginComponent。
    ⚠️ 順序很重要：必須放在最後，否則會攔截所有路由。
*/


/*
內容解釋
1. Initial Chunk Files（初始載入檔案）
        這些是應用程式在第一次載入時就需要的檔案：
          vendor.js (3.19 MB)
          => 包含第三方套件（例如 Angular 核心、RxJS、Bootstrap 等）。通常是最大的一塊。
          main.js (439.76 kB)
          => 你的應用程式主要程式碼（元件、模組、服務）。
          styles.css / styles.js (353.55 kB)
          => 全域樣式檔案。
          polyfills.js (234.36 kB)
          => 瀏覽器相容性補丁，讓舊版瀏覽器能支援新語法。
          scripts.js (78.65 kB)
          => 額外引入的 JavaScript（可能是第三方外掛）。
          runtime.js (14.13 kB)
          => Angular 啟動程式碼，負責載入其他 chunk。
          👉 Initial Total = 4.29 MB
          這是第一次載入應用程式時需要`下載的總大小。

2. Lazy Chunk Files（延遲載入檔案）
        這些檔案只有在需要時才會載入（Lazy Loading），能減少初始載入時間：
          canvg (434.97 kB)
          => 用來把 SVG 轉成 Canvas 的套件。
          html2canvas (377.67 kB)
          => 把 HTML 元素轉成 Canvas 圖像的套件。
          component-profile-user-module (104.08 kB)
          => 你的 UserModule，因為用了 Lazy Loading，所以獨立成一個 chunk。
          dompurify (63.27 kB)
          => 用來清理 HTML，避免 XSS 攻擊的套件。

3. 為什麼要分 Initial 與 Lazy ?
      Initial Chunk：必須在第一次載入就下載，確保應用程式能啟動。
      Lazy Chunk：只有在使用者進入相關頁面或功能時才下載，提升效能。

✅ 總結：
    這份輸出顯示 Angular 應用程式的打包結果。
    初始載入需要 4.29 MB。
    其他功能模組（例如 UserModule、第三方套件）會在需要時才載入。
    這樣的設計能讓應用程式更快啟動，並且減少不必要的下載。
 */
const routes: Routes = [
  // Lazy Loading : loadChildren 的寫法正確，會在進入 /profile 路徑時載入 UserModule。
  //Lazy Loading 的設定正確，子模組要用 path: '' 來接上 /profile。
  { path: 'profile', loadChildren: () => import('./component/profile/user.module').then(module => module.UserModule)},
  /*
  📌 拆解說明 :
(1) { path: '', component: HomeComponent }
    當路由是空字串 (http://localhost:4200/)，會顯示 HomeComponent。
    這是 Angular 裡的「預設路由 (default route)」。

(2) { path: '', redirectTo: '/', pathMatch: 'full' }
    同樣是空字串路由。
    這裡設定的是「當路由是空字串時，重新導向到 /」。
    但 / 本身又會對應到空字串路由 → 造成迴圈或衝突。

(3) pathMatch: 'full'
    表示必須完全匹配整個 URL 路徑，路由才會生效。
  */
  { path: '', redirectTo: '/', pathMatch: 'full' },

  // enter any url doesn't exist, then go to the HomeComponent, and then it is also protected
  { path: '**', component: HomeComponent, canActivate: [AuthenticationGuard]  } // Order matters here. It is important that this "catch all" path is the last item in the array.
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })], // (app.module.ts) 在 Angular 裡，AppRoutingModule 是一個專門用來管理 應用程式路由 (Routing) 的模組。它的主要用途是把不同的 URL 路徑對應到不同的元件 (Component)，讓使用者在瀏覽器中切換頁面時，Angular 能顯示正確的內容。
  exports: [RouterModule]
})
export class AppRoutingModule { }

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
