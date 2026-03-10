import { NgModule } from '@angular/core';
import { NotifierModule, NotifierOptions } from 'angular-notifier';


//這段程式碼是一個 Angular Notifier 套件的設定物件 (NotifierOptions)，用來控制通知訊息在畫面上的顯示方式。
const notificationConfig: NotifierOptions = {
  position: {
    horizontal: {
      position: 'middle',
      distance: 150 //距離螢幕邊界 150px
    },
    vertical: {
      position: 'top',
      distance: 90, //距離頂端 12px
      gap: 10 //多個通知之間的間距 10px
    }
  },

  //📌 行為 (behaviour)
  theme: 'material', //使用 Material Design 風格的通知樣式。
  behaviour: {
    autoHide: 6000, //通知顯示 7 秒後自動隱藏
    onClick: 'hide',//點擊通知時隱藏
    onMouseover: 'pauseAutoHide',//滑鼠移到通知上時暫停自動隱藏
    showDismissButton: true, //顯示「關閉」按鈕
    stacking: 4 //最多同時顯示 4 個通知
  },

  //📌 動畫效果 (animations)
  animations: {
    enabled: true,//enabled: true → 啟用動畫
    show: {//show：通知顯示時的動畫
      preset: 'slide',//preset: 'slide' → 滑入效果
      speed: 300,//speed: 300 → 動畫速度 300ms
      easing: 'ease'//easing: 'ease' → 緩動效果
    },
    hide: {//easing: 'ease' → 緩動效果
      preset: 'fade',//preset: 'fade' → 淡出效果
      speed: 300,//speed: 300 → 動畫速度 300ms
      easing: 'ease',
      offset: 50 //offset: 50 → 位移 50px
    },
    shift: {//shift：多個通知移動時的動畫

      speed: 300,
      easing: 'ease'
    },
    overlap: 150
  }
};

@NgModule({
// NotifierModule.withConfig({})這裡可以傳入設定物件，例如通知的位置、動畫效果、顯示時間等。如果留空 {}，就會使用預設設定。
  imports: [ NotifierModule.withConfig(notificationConfig)],

//exports: [NotifierModule] 這樣做的好處是：當其他模組（例如 AppModule 或 Feature Module）匯入 NotificationModule 時，就能直接使用 NotifierModule 的功能，而不用每次都重複匯入。
  exports: [ NotifierModule ]
})
export class NotificationModule {}
