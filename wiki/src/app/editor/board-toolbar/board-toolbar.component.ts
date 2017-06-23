import {AfterContentInit, AfterViewInit, Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {BoardService} from "../../service/board.service";
import {LocalStorage, LocalStorageService} from "ngx-webstorage";
import {ArticleService} from "../../service/article.service";
import {Article} from "../../model/article";
import {BlockUIService} from "ng-block-ui";
import 'rxjs/Rx';
import {Observable} from "rxjs/Observable";
import {AppConstants} from "../../service/app-constants";

@Component({
  selector: 'board-toolbar',
  templateUrl: './board-toolbar.component.html',
  styleUrls: ['./board-toolbar.component.css']
})
export class BoardToolbarComponent implements OnInit {

  @ViewChild('selectColor') selectColor: ElementRef;
  @ViewChild('selectType') selectType: ElementRef;
  @LocalStorage() addBlack: boolean;
  @LocalStorage() addQueen: boolean;
  @LocalStorage(AppConstants.EDIT_MODE_STORAGE_KEY) editMode: boolean;
  observableArticles: Observable<Article[]>;
  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;
  @LocalStorage(AppConstants.REMOVE_DRAUGHT_STORAGE_KEY) removeDraught: boolean;
  @LocalStorage(AppConstants.SELECTED_DRAUGHT_DESC_STORAGE_KEY) selectedDraughtDesc: { black: boolean, queen: boolean };

  constructor(private articleService: ArticleService) {
  }

  ngOnInit() {
  }

  onDraughtSelected(black: boolean, queen: boolean) {
    this.addQueen = queen;
    this.addBlack = black;
    this.selectedDraughtDesc = {black: this.addBlack, queen: this.addQueen};
    this.removeDraught = false;
  }

  onRemoveDraught() {
    this.selectedDraughtDesc = null;
    this.removeDraught = true;
  }

  onToggleEditMode() {
    this.editMode = !this.editMode;
  }

  findArticles() {
    this.observableArticles = this.articleService.findArticles();
  }

  updateArticle() {
    this.articleService.updateArticle();
  }

  fillInitBoard() {
    this.articleService.fillInBoard();
  }

  eraseBoard() {
    this.articleService.eraseBoard();
  }

  removeArticle() {
    this.articleService.removeArticle();
  }
}
