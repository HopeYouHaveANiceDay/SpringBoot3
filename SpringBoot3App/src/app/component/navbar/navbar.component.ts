import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { User } from 'src/app/interface/user';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NavbarComponent {
  @Input() user: User;

  constructor(private router: Router, private userService: UserService, private notificationService: NotificationService){}

  logOut(): void {
    this.userService.logOut();
    this.router.navigate(['/login']);
    // 這是通知要顯示的文字內容。因為字串裡有 '，所以用 \' 來跳脫。
    // 最後顯示的訊息就是：You've been successfully logged out
    this.notificationService.onDefault('You\'ve been successfully logged out');
  }
}
