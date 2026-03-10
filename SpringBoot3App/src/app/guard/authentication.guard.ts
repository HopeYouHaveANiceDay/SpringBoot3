import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { UserService } from '../service/user.service';

/*
@Injectable({ providedIn: 'root' }) :
  讓這個 Service 在整個 Angular 應用程式中都是單例 (singleton)，不需要額外在 app.module.ts 註冊。

單例模式 (Singleton Pattern) :
  定義：確保一個類別 (Class) 在應用程式中只存在一個實例 (Instance)。
  作用：提供一個全域存取點，常用於管理共享資源（如資料庫連接、配置設定）。
  目的：避免重複建立物件帶來的系統開銷。
*/
@Injectable({
  providedIn: 'root'
})
/*
@Injectable({ providedIn: 'root' })
    表示這個 UserService 是一個可被依賴注入 (Dependency Injection) 的服務。
    providedIn: 'root' 意味著它會在整個應用程式中以單例模式存在。   */


export class AuthenticationGuard {


/* 建構子 (constructor) :
      這裡注入了兩個依賴：
        userService：用來檢查使用者是否已登入。
        router：Angular 的路由器，用來做頁面導向。
      */
  constructor(private userService: UserService, private router: Router) {}


/*
  canActivate 方法 :
      這是 Angular 路由守衛 (Route Guard) 的方法。
      當使用者要進入某個路由時，Angular 會呼叫 canActivate。
      如果回傳 true → 允許進入。
      如果回傳 false → 阻止進入。       */
  canActivate(routeSnapShot: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.isAuthenticated();
  }


/* isAuthenticated 方法 :
      檢查使用者是否已登入。
      如果 userService.isAuthenticated() 回傳 true → 表示已登入，允許進入。
      如果不是 → 導向 /login 頁面，並回傳 false。*/
  private isAuthenticated(): boolean {
        if (this.userService.isAuthenticated()) {
          return true;
        } else {
            this.router.navigate(['/login']);
            return false;
        }
  }
}
