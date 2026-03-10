import { ChangeDetectionStrategy, Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Observable, BehaviorSubject, map, startWith, catchError, of, switchMap } from 'rxjs';
import { DataState } from 'src/app/enum/datastate.enum';
import { AccountType, CustomHttpResponse, Page, VerifyState } from 'src/app/interface/appstates';
import { User } from 'src/app/interface/user';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-verify',
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class VerifyComponent implements OnInit {

  //verifyState$：用來追蹤驗證流程的狀態（成功、失敗、載入中）。
  verifyState$: Observable<VerifyState>;

  //userSubject / user$：用 BehaviorSubject 管理使用者資料，並轉成 Observable 供模板訂閱。
  private userSubject = new BehaviorSubject<User>(null);
  user$ = this.userSubject.asObservable();

  //isLoadingSubject / isLoading$：管理載入狀態。
  private isLoadingSubject = new BehaviorSubject<boolean>(false)
  isLoading$ = this.isLoadingSubject.asObservable();

  //DataState：引用 enum，方便在模板中使用。
  readonly DataState = DataState;

  //ACCOUNT_KEY：固定字串 'key'，對應路由參數名稱。
  private readonly ACCOUNT_KEY: string = 'key';

  //📌 we need know which route was activated
  // 1. ActivatedRoute：用來存取目前路由的資訊（例如 :key 參數）。
  // 2. UserService：用來呼叫後端 API 進行驗證。
  constructor(private activatedRoute: ActivatedRoute, private userService: UserService, private notificationService: NotificationService) { }

  ngOnInit(): void {

    // 1. activatedRoute.paramMap 監聽路由參數的變化，取得 :key 的值。
    this.verifyState$ = this.activatedRoute.paramMap.pipe(

      // 2. switchMap 把路由參數轉換成另一個 Observable，這裡是呼叫 userService.verify$。
      switchMap((params: ParamMap) => {
        console.log(this.activatedRoute);

       /* 1. window.location.href
                  取得目前瀏覽器網址的完整字串，例如：
                  http://localhost:4200/user/verify/account/12345
                  http://localhost:4200/user/verify/password/abcdef

          2. this.getAccountType(...)
                呼叫元件裡的私有方法 getAccountType，並傳入目前網址。這個方法會根據網址判斷驗證的類型：
                如果網址包含字串 "password" → 回傳 'password'
                否則 → 回傳 'account'

          3. const type: AccountType
                宣告一個變數 type，型別是 AccountType（一個 enum 或 type alias），用來表示目前要驗證的是「帳號」還是「密碼」。

          為什麼要這樣做 ?
                這樣的設計是因為同一個 VerifyComponent 同時處理兩種驗證路由：
                /user/verify/account/:key → 驗證帳號
                /user/verify/password/:key → 驗證密碼
                透過檢查網址，就能決定要呼叫後端 API 時傳入的驗證類型，避免寫兩個不同的元件。
            */
        const type: AccountType = this.getAccountType(window.location.href);

        // 3. userService.verify$ 傳入 key 和 type（判斷是驗證帳號還是密碼），呼叫後端 API。
        return this.userService.verify$(params.get(this.ACCOUNT_KEY), type) //teh key is UUID randon generated. the key comes from the ':key' in routes in app-routing.module.ts.
          .pipe(

            // 4. map(response => {...})
            //    如果是密碼驗證，更新 userSubject。
            //    回傳一個 VerifyState 物件，表示驗證成功。
            map(response => {
              this.notificationService.onDefault(response.message);
              console.log(response);
              type === 'password' ? this.userSubject.next(response.data.user) : null;
              // verifySuccess comes from interface VerifyState in appstates.ts.
              return { type, title: 'Verified!', dataState: DataState.LOADED, message: response.message, verifySuccess: true };
            }),

            // 5. startWith(...) 在 API 回應之前，先回傳一個「載入中」的狀態。
            startWith({ title: 'Verifying...', dataState: DataState.LOADING, message: 'Please wait while we verify the information', verifySuccess: false }),

            // 6. catchError(...) 如果 API 發生錯誤，回傳一個「錯誤」的狀態。
            catchError((error: string) => {
              this.notificationService.onError(error);
              return of({ title: error, dataState: DataState.ERROR, error, message: error, verifySuccess: false })
            })
          )
      })
    );
  }



  renewPassword(resetPasswordform: NgForm): void {
    this.isLoadingSubject.next(true);
    // 1. activatedRoute.paramMap 監聽路由參數的變化，取得 :key 的值。
    this.verifyState$ = this.userService.renewPassword$
      ({
        userId: this.userSubject.value.id,
        password: resetPasswordform.value.password,
        confirmPassword: resetPasswordform.value.confirmPassword
      }) //teh key is UUID randon generated. the key comes from the ':key' in routes in app-routing.module.ts.
          .pipe(


            map(response => {
              this.notificationService.onDefault(response.message);
              console.log(response);
              //type === 'password' ? this.userSubject.next(response.data.user) : null;
              this.isLoadingSubject.next(false);

              return { type: 'account' as AccountType, title: 'Success', dataState: DataState.LOADED, message: response.message, verifySuccess: true };
            }),


            startWith({ type: 'password' as AccountType, title: 'Verified!', dataState: DataState.LOADED, verifySuccess: false }),


            catchError((error: string) => {
              this.notificationService.onError(error);
              this.isLoadingSubject.next(false);
              return of({ type: 'password' as AccountType, title: 'Verified!', dataState: DataState.LOADED, error, verifySuccess: true })
            })
          )
  }

  // 根據 URL 判斷驗證類型：如果路徑包含 password，就是密碼驗證；否則就是帳號驗證。
  private getAccountType(url: string): AccountType {
    return url.includes('password') ? 'password' : 'account';
  }
}
