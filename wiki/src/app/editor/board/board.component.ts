import {Component, Input, OnInit} from '@angular/core';
import {Board} from "../../model/board";
import {BoardService} from "../../service/board.service";
import {Utils} from "../../service/utils.service";
import "rxjs/add/observable/of";
import {LocalStorageService} from "ngx-webstorage";
import {ArticleService} from "../../service/article.service";

@Component({
  selector: 'board',
  templateUrl: './board.component.html',
  styleUrls: ['./board.component.css']
})
export class BoardComponent implements OnInit {

  boardDimension: Array<number>;
  boardChunk: Array<number>;
  board: Board;

  constructor(private articleService: ArticleService) {
  }

  ngOnInit() {
    this.articleService.articleObservable().subscribe((article) => {
      if (article) {
        this.updateBoard(article.board);
      }
    });
    if (this.articleService.article) {
      this.updateBoard(this.articleService.article.board);
    }
  }

  private updateBoard(board) {
    if (board) {
      this.boardDimension = Utils.getBoardLenth(board.rules);
      this.boardChunk = Utils.getChunk(this.boardDimension, Math.abs(board.rules));
      this.board = board;
    }
  }

}
