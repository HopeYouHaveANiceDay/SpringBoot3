import { Injectable } from '@angular/core';
import { NotifierService } from 'angular-notifier';

/*
📌 語言
這是 TypeScript 程式碼。
TypeScript 是 JavaScript 的超集，支援型別系統與裝飾器 (decorators)，非常適合用在 Angular 專案。
*/


/*
@Injectable({ providedIn: 'root' }) :
  讓這個 Service 在整個 Angular 應用程式中都是單例 (singleton)，不需要額外在 app.module.ts 註冊。

單例模式 (Singleton Pattern) :
  定義：確保一個類別 (Class) 在應用程式中只存在一個實例 (Instance)。
  作用：提供一個全域存取點，常用於管理共享資源（如資料庫連接、配置設定）。
  目的：避免重複建立物件帶來的系統開銷。
*/

/*
在你的程式碼裡出現的 NotifierService 和 notify，其實是來自套件 angular-notifier 的核心功能。讓我拆解一下：
🔑 NotifierService
    這是一個由 angular-notifier 提供的 服務 (Service)。
    它的用途是讓你在程式裡呼叫通知，而不用直接操作 UI。
    你只要在元件或服務裡注入它，就能透過它來顯示通知訊息。

🔔 notify 方法
    notify 是 NotifierService 提供的方法，用來觸發通知。
*/
@Injectable()
export class NotificationService {
  private readonly notifier: NotifierService; //we need NotifierService, not NotificationService

  constructor(notificationService: NotifierService) {
    this.notifier = notificationService;
  }

  onDefault(message: string): void {
    this.notifier.notify(Type.DEFAULT, message);
  }

  onSuccess(message: string): void {
    this.notifier.notify(Type.SUCCESS, message);
  }

  onInfo(message: string): void {
    this.notifier.notify(Type.INFO, message);
  }

  onWarning(message: string): void {
    this.notifier.notify(Type.WARNING, message);
  }

  onError(message: string): void {
    this.notifier.notify(Type.ERROR, message);
  }
}

enum Type {
  DEFAULT = 'default',
  INFO = 'info',
  SUCCESS = 'success',
  WARNING = 'warning',
  ERROR = 'error'
}
