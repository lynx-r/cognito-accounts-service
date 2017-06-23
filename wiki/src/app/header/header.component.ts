import { Component, OnInit } from '@angular/core';
import {LocalStorage} from "ngx-webstorage";
import {AppConstants} from "../service/app-constants";
import {User} from "../model/user";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {

  @LocalStorage(AppConstants.LOGGED_IN_STORAGE_KEY) isLoggedIn: boolean;
  @LocalStorage(AppConstants.USER_STORAGE_KEY) user: User;

  constructor() { }

  ngOnInit() {
  }

}
