import {Injectable} from '@angular/core';
import {Http, RequestOptions} from "@angular/http";
import {AppConstants} from "./app-constants";
import {Observable} from "rxjs/Observable";
import {LocalStorage} from "ngx-webstorage";
import {Utils} from "./utils.service";
import {Router} from "@angular/router";
import config from "../config/config.json";

@Injectable()
export class UserService {
  @LocalStorage(AppConstants.LOGGED_IN_STORAGE_KEY) isLoggedIn: boolean;
  redirectUrl: string;

  constructor(private http: Http,
              private router: Router) {
  }

  register(param: { given_name: string, email: string, password: string }): Observable<any> {
    return this.http.get((<any>config).api_url + AppConstants.USERS_REQUEST + '/logout', param).map((resp) => {
      const body = resp.json();
      if (body.ok) {
        this.router.navigate([this.redirectUrl ? this.redirectUrl : 'home']);
        this.isLoggedIn = true;
        return body.data;
      } else {
        Utils.handleError(body.comment);
      }
    });
  }

  authenticate(param: { email; password }) {
    let options = new RequestOptions({withCredentials: true});
    return this.http.post((<any>config).api_url + AppConstants.USERS_REQUEST + '/authenticate', param).map((resp) => {
      const body = resp.json();
      if (body.ok) {
        this.isLoggedIn = true;
        this.router.navigate([this.redirectUrl ? this.redirectUrl : 'home']);
        return body.data;
      } else {
        Utils.handleError(body.comment);
      }
    })
  }

  logout(param: { email: string }) {
    return this.http.post((<any>config).api_url + AppConstants.USERS_REQUEST + '/logout', param).do((resp) => {
      const body = resp.json();
      if (body.ok) {
        this.isLoggedIn = false;
        this.router.navigate(['passport']);
      } else {
        Utils.handleError(body.comment);
      }
    });
  }

}
