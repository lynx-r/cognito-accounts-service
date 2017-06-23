import { Injectable } from '@angular/core';
import {Board} from "../model/board";
import * as _ from 'lodash';
import {Observable} from "rxjs/Observable";

@Injectable()
export class Utils {

  constructor() { }

  public static randomString(length: number = 5): string {
    return Math.random().toString(36).substring(2, 2 + length);
  }

  static handleError(error: any) {
    console.log(error);
    Observable.throw(error);
  }

  static handleResponse(resp: any): Promise<any> {
    return new Promise((resolve, reject) => {
      if (resp.ok) {
        resolve(resp);
      } else {
        Utils.handleError(resp.comment);
        reject(resp.comment);
      }
    })
  }

  static getBoardLenth(rules: number) {
    return _.range(Math.abs(<number>rules * <number>rules));
  }

  static getChunk(boardDimension: Array<number>, length: number) {
    return _.chunk(boardDimension, length);
  }
}
