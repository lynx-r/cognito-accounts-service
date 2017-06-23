import {Injectable} from '@angular/core';
import {SocketService} from "./socket.service";
import {LocalStorage, LocalStorageService} from "ngx-webstorage";
import {Article} from "../model/article";
import {Utils} from "./utils.service";
import {Observable} from "rxjs/Observable";
import {AppConstants} from "./app-constants";
import {BlockUIService} from "ng-block-ui";
import {Http} from "@angular/http";
import config from "../config/config.json";
import {Response} from "../http/response";
import {EmptyObservable} from "rxjs/observable/EmptyObservable";

@Injectable()
export class ArticleService {

  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;

  constructor(private localStorage: LocalStorageService,
              private socketService: SocketService,
              private http: Http,
              private blockUIService: BlockUIService) {
  }

  createArticle(config) {
    this.blockUI((unblock: Function) => {
      this.socketService.socket.emit(AppConstants.CREATE_ARTICLE, config, (resp) => {
        if (resp.ok) {
          this.refreshArticle(resp);
        } else {
          Utils.handleError(resp.comment);
        }
        unblock();
      })
    })
  }

  private blockUI(callback: Function) {
    this.blockUIService.start(AppConstants.BLOCK_MAIN);
    callback(() => {
      this.blockUIService.stop(AppConstants.BLOCK_MAIN);
    });
  }

  refreshArticle(resp) {
    this.localStorage.clear(AppConstants.ARTICLE_STORAGE_KEY);
    if (resp) {
      this.localStorage.store(AppConstants.ARTICLE_STORAGE_KEY, resp.data);
      this.localStorage.store(AppConstants.BOARD_STORAGE_KEY, resp.data.board);
    }
  }

  findArticles(): Observable<Article[]> {
    return this.http.get((<any>config).api_url + AppConstants.ARTICLES_REQUEST).map((resp) => {
      const body: Response = <Response>JSON.parse(atob(resp.text()));
      // "Find failed AccessDeniedException: User: arn:aws:sts::089753065094:assumed-role/ShashkiOnlineAPI-dev-eu-central-1-lambdaRole/ShashkiOnlineAPI-dev-dist is not authorized to perform: dynamodb:Scan on resource: arn:aws:dynamodb:us-west-1:089753065094:table/Article"
      if (body.ok) {
        return body.data;
      }
      return new EmptyObservable();
    });
  }

  findArticlesIO(): Observable<Article[]> {
    return new Observable((observer) => {
      this.socketService.socket.emit(AppConstants.FIND_ARTICLES, {}, (resp) => {
        if (resp.ok) {
          observer.next(resp.data);
          observer.complete();
        } else {
          Utils.handleError(resp.comment);
          observer.error(resp.comment);
        }
      })
    }).do(() => {
      this.blockUIService.stop(AppConstants.BLOCK_MAIN);
    })
  }

  openArticle(config: {_id: string}) {
    this.blockUI((unblock) => {
      this.socketService.socket.emit(AppConstants.FIND_ARTICLE, config, (resp) => {
        if (resp.ok) {
          this.refreshArticle(resp);
        } else {
          Utils.handleError(resp.comment);
        }
        unblock();
      });
    })
  }

  articleObservable() {
    return this.localStorage.observe(AppConstants.ARTICLE_STORAGE_KEY);
  }

  updateArticle() {
    this.blockUI((unblock) => {
      this.socketService.socket.emit(AppConstants.UPDATE_ARTICLE, {article: this.article}, (resp) => {
        if (resp.ok) {
          this.refreshArticle(resp);
        } else {
          Utils.handleError(resp.comment);
        }
        unblock();
      })
    })
  }

  fillInBoard() {
    this.blockUI((unblock) => {
      this.socketService.socket.emit(AppConstants.FILL_IN_BOARD, {_id: this.article._id}, (resp) => {
        if (resp.ok) {
          this.refreshArticle(resp);
        } else {
          Utils.handleError(resp.comment);
        }
        unblock();
      })
    })
  }

  eraseBoard() {
    this.blockUI((unblock) => {
      this.socketService.socket.emit(AppConstants.CLEAR_BOARD, {_id: this.article._id}, (resp) => {
        if (resp.ok) {
          this.refreshArticle(resp);
        } else {
          Utils.handleError(resp.data);
        }
        unblock();
      })
    })
  }

  removeArticle() {
    if (!this.article) {
      return;
    }
    this.blockUI((unblock) => {
      this.socketService.socket.emit(AppConstants.REMOVE_ARTICLE, {_id: this.article._id}, (resp) => {
        if (resp.ok) {
          this.refreshArticle(null);
        } else {
          Utils.handleError(resp.data);
        }
        unblock();
      })
    })
  }

  get board() {
    return this.article.board;
  }
}
