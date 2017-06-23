import {ElementRef, EventEmitter, Injectable, Output} from '@angular/core';
import {Square} from "../model/square";
import {Board} from "../model/board";
import {Utils} from "./utils.service";
import {SocketService} from "./socket.service";
import {LocalStorage, LocalStorageService} from "ngx-webstorage";
import {Draught} from "../model/draught";
import {ArticleService} from "./article.service";
import {AppConstants} from "./app-constants";
import {Article} from "../model/article";

@Injectable()
export class BoardService {

  @LocalStorage(AppConstants.BOARD_STORAGE_KEY) board: Board;
  @LocalStorage(AppConstants.EDIT_MODE_STORAGE_KEY) editModeFlag: boolean;
  @LocalStorage(AppConstants.SELECTED_DRAUGHT_DESC_STORAGE_KEY) selectedDraughtDescFlag: { black: boolean, queen: boolean };
  @LocalStorage(AppConstants.REMOVE_DRAUGHT_STORAGE_KEY) removeDraughtFlag: boolean;

  @Output() squareClickedEvent = new EventEmitter<Square>();
  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;

  constructor(private articleService: ArticleService, private socketService: SocketService, private localStorage: LocalStorageService) {
  }

  observableSelectedDraughtDesc(): EventEmitter<{ black: boolean, queen: boolean }> {
    return this.localStorage.observe(AppConstants.SELECTED_DRAUGHT_DESC_STORAGE_KEY);
  }
  //
  // get selectedDraughtDescFlag() {
  //   return this._selectedDraughtDesc;
  // }
  //
  // set selectedDraughtDescFlag(flag) {
  //   this.localStorage.store(AppConstants.SELECTED_DRAUGHT_DESC_STORAGE_KEY, flag);
  // }

  observableEditMode(): EventEmitter<boolean> {
    return this.localStorage.observe(AppConstants.EDIT_MODE_STORAGE_KEY);
  }

  // get editModeFlag() {
  //   return this._editMode;
  // }
  //
  // set editModeFlag(mode){
  //   this.localStorage.store(AppConstants.EDIT_MODE_STORAGE_KEY, mode);
  // }

  resetHighlight() {
    this.goThroughBoard((square) => {
      if (square.black) {
        square.highlight = false;
      }
    })
  }

  highlightAllowed(allowedSquares: Square[]) {
    this.goThroughBoard((square) => {
      for (let hl of allowedSquares) {
        if (square.x == hl.x && square.y == hl.y) {
          square.highlight = true;
        }
      }
    })
  }

  private goThroughBoard(func: Function) {
    for (let square of this.articleService.board.board) {
      func(square);
    }
  }

  moveDraught(sourceSquare: Square, draught: Draught, targetSquare: Square, queen: boolean) {
    sourceSquare.draught = null;
    sourceSquare.occupied = false;
    draught.x = targetSquare.x;
    draught.y = targetSquare.y;
    draught.queen = queen;
    targetSquare.draught = draught;
    targetSquare.occupied = true;
  }

  beatDraughts(beaten: Square[]) {
    this.goThroughBoard((square) => {
      for (let s of beaten) {
        if (s.x == square.x && s.y == square.y) {
          square.draught = null;
          square.occupied = false;
        }
      }
    })
  }

  addDraught(param: { _articleId: string, x: number; y: number; black: boolean; queen: boolean }) {
    this.socketService.socket.emit(AppConstants.ADD_DRAUGHT, param, (resp) => {
      Utils.handleResponse(resp).then((resp) => {
        this.article = resp.data;
      })
    })
  }

  removeDraught(param: { _articleId: string, x: number, y: number }) {
    this.socketService.socket.emit(AppConstants.REMOVE_DRAUGHT, param, (resp) => {
      Utils.handleResponse(resp).then((resp) => {
        this.articleService.article = resp.data;
      })
    })
  }

  observableRemoveDraughtFlag() {
    return this.localStorage.observe(AppConstants.REMOVE_DRAUGHT_STORAGE_KEY);
  }
  //
  // set removeDraughtFlag(flag) {
  //   this.localStorage.store(AppConstants.REMOVE_DRAUGHT_STORAGE_KEY, flag);
  // }
  //
  // get removeDraughtFlag() {
  //   return this._removeDraught;
  // }

  highlightAllowedFor(config: { _articleId: string; draught: Draught }, callback: (resp) => any) {
    this.socketService.socket.emit(AppConstants.HIGHLIGHT, config, (resp) => {
      this.resetHighlight();
      this.highlightAllowed(resp.data.highlighted.allow);
      callback(resp);
    })
  }

  /**
   * spaghetti for DraughtComponent
   * @returns {Article}
   */
  activeArticle(): Article {
    return this.articleService.article;
  }

  moveDraughtTo(config: { _articleId: string; allowedSquares: Square[]; beatenPos: Square[]; square: Square }, callback: (resp) => any) {
    this.socketService.socket.emit(AppConstants.MOVE_DRAUGHT_TO, config, (resp) => {
      callback(resp);
    });
  }

}
