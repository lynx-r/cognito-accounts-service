import {Injectable} from '@angular/core';
import {BaseRequestOptions, RequestOptions, Headers} from '@angular/http';
import {LocalStorage} from "ngx-webstorage";
import {AppConstants} from "./app-constants";

@Injectable()
export class DefaultRequestOptions extends BaseRequestOptions {

  @LocalStorage(AppConstants.AUTHORIZATION_STORAGE_KEY) private authToken: string;

  private superHeaders: Headers;

  constructor() {
    super();
    this.superHeaders.set('Access-Control-Allow-Origin', AppConstants.HOST)
    this.superHeaders.set('Access-Control-Allow-Headers', 'x-requested-with')
    this.superHeaders.set('Content-Type', 'application/json')
  }

  get headers() {
    if (this.authToken) {
      this.superHeaders.set('Authorization', `Bearer ${this.authToken}`);
    } else {
      this.superHeaders.delete('Authorization');
    }
    return this.superHeaders;
  }

  set headers(headers: Headers) {
    this.superHeaders = headers;
  }

}

export const requestOptionsProvider = {provide: RequestOptions, useClass: DefaultRequestOptions};
