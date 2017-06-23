import {Component, Input, OnInit} from '@angular/core';
import {Square} from "../../../model/square";
import {BoardService} from "../../../service/board.service";
import {ArticleService} from "../../../service/article.service";
import {LocalStorage} from "ngx-webstorage";
import {AppConstants} from "../../../service/app-constants";
import {Article} from "../../../model/article";

@Component({
  selector: 'square',
  templateUrl: './square.component.html',
  styleUrls: ['./square.component.css']
})
export class SquareComponent implements OnInit {

  @Input() square: Square;
  draughtTop: string;
  draughtLeft: string;
  black: boolean;
  @LocalStorage(AppConstants.REMOVE_DRAUGHT_STORAGE_KEY) removeDraught: boolean;
  @LocalStorage(AppConstants.EDIT_MODE_STORAGE_KEY) editMode: boolean;
  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;
  // private removeDraught: boolean;
  // private editMode: boolean;

  constructor(private boardService: BoardService) {
  }

  ngOnInit() {
    if (this.square) {
      this.black = this.square.black;
      // берем 10% от размера клетки
      this.draughtTop = this.draughtLeft = `${this.square.size / 10}px`;
    }
  }

  onSquareClicked() {
    if (this.removeDraught) {
      this.boardService.removeDraught({
        _articleId: this.article._id,
        x: this.square.x,
        y: this.square.y
      });
    } else {
      if (this.editMode) {
        this.boardService.addDraught({
          _articleId: this.article._id,
          x: this.square.x,
          y: this.square.y,
          black: this.boardService.selectedDraughtDescFlag.black,
          queen: this.boardService.selectedDraughtDescFlag.queen
        });
      } else {
        this.boardService.squareClickedEvent.emit(this.square);
      }
    }
  }

}
