import {Component, OnInit} from '@angular/core';
import {UserService} from "../service/user.service";
import {LocalStorage} from "ngx-webstorage";
import {AppConstants} from "../service/app-constants";
import {ActivatedRoute, Router} from "@angular/router";
import {User} from "../model/user";

@Component({
  selector: 'app-passport',
  templateUrl: './passport.component.html',
  styleUrls: ['./passport.component.css']
})
export class PassportComponent implements OnInit {

  @LocalStorage(AppConstants.USER_STORAGE_KEY) user: User;

  constructor(private route: ActivatedRoute, private userService: UserService) {
  }

  ngOnInit() {
    this.route
      .queryParams
      .subscribe((params) => {
        if (params['logout']) {
          console.log(params);
          this.userService.logout({email: this.user.email})
            .subscribe(() => {
              this.user = null;
            });
        }
      });
  }

  onSignUp(name, email, password) {
    this.userService
      .register({given_name: name.value, email: email.value, password: password.value})
      .subscribe((user) => {
        this.user = user;
      });
  }

  onLogin(email, password) {
    this.userService
      .authenticate({email: email.value, password: password.value})
      .subscribe((user) => {
        this.user = user;
        console.log(user);
      });
  }

}
