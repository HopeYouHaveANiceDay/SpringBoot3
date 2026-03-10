import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Observable, of, map, startWith, catchError } from 'rxjs';
import { DataState } from 'src/app/enum/datastate.enum';
import { RegisterState } from 'src/app/interface/appstates';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-resetpassword',
  templateUrl: './resetpassword.component.html',
  styleUrls: ['./resetpassword.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ResetpasswordComponent {
  resetPasswordState$: Observable<RegisterState> = of({ dataState: DataState.LOADED });

  //used for temple "html"
  //e.g, *ngIf="state.dataState === DataState.ERROR"
/* 不能直接在 HTML 用 enum，是因為 Angular 模板只認得元件的屬性與方法，不認得 TypeScript 的 enum。
  透過 readonly DataState = DataState; 把 enum 掛到元件上，就能在模板裡安全地使用它。 */
  readonly DataState = DataState;

  constructor(private userService: UserService, private notificationService: NotificationService) {}

  resetPassword(resetPasswordForm: NgForm): void {
    //把表單裡的 email 傳給後端服務，if the email exist in the database, send an email for them to click the link to confirm the email to reset the password
    this.resetPasswordState$ = this.userService.requestPasswordReset$(resetPasswordForm.value.email)//in this case, we only need the email
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response); //log it for debug
          resetPasswordForm.reset();
          return { dataState: DataState.LOADED, registerSuccess: true, message: response.message };
        }),
        startWith({ dataState: DataState.LOADING, registerSuccess: false}),
        catchError((error: string) => {
          this.notificationService.onError(error);
          return of({ dataState: DataState.ERROR, registerSuccess: false, error })
        })
      );
  }
}
