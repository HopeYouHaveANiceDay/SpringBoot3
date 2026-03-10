import { ChangeDetectionStrategy, Component } from '@angular/core';
import { NgForm } from '@angular/forms';
import { catchError, map, Observable, of, pipe, startWith } from 'rxjs';
import { DataState } from 'src/app/enum/datastate.enum';
import { RegisterState } from 'src/app/interface/appstates';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterComponent {
  registerState$: Observable<RegisterState> = of({ dataState: DataState.LOADED });

  //used for temple "html"
  //e.g, *ngIf="state.dataState === DataState.ERROR"
/* 不能直接在 HTML 用 enum，是因為 Angular 模板只認得元件的屬性與方法，不認得 TypeScript 的 enum。
  透過 readonly DataState = DataState; 把 enum 掛到元件上，就能在模板裡安全地使用它。 */
  readonly DataState = DataState;

  constructor(private userService: UserService, private notificationService: NotificationService) {}

  register(registerForm: NgForm): void {
    this.registerState$ = this.userService.save$(registerForm.value)
      .pipe(
        map(response => {
          this.notificationService.onDefault(response.message);
          console.log(response); //log it for debug
          registerForm.reset();
          return { dataState: DataState.LOADED, registerSuccess: true, message: response.message };
        }),
        startWith({ dataState: DataState.LOADING, registerSuccess: false}),
        catchError((error: string) => {
          this.notificationService.onError(error);
          return of({ dataState: DataState.ERROR, registerSuccess: false, error })
        })
      );
  }

  createAccountForm(): void {
    this.registerState$ = of({ dataState: DataState.LOADED, registerSuccess: false });
  }
}
