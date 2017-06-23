import {Component, Input, OnInit} from '@angular/core';
import {ArticleService} from "../../../service/article.service";
import {Article} from "../../../model/article";
import {Observable} from "rxjs/Observable";
import {BlockUIService} from "ng-block-ui";

@Component({
  selector: 'open-article-dialog',
  templateUrl: './open-article-dialog.component.html',
  styleUrls: ['./open-article-dialog.component.css']
})
export class OpenArticleDialogComponent implements OnInit {

  @Input() observableArticles: Observable<Article[]>;
  selectedArticle: Article;

  constructor(private articleService: ArticleService, private blockUIService: BlockUIService) {
  }

  ngOnInit() {
    this.articleService.articleObservable().subscribe((article) => {
      this.selectedArticle = article;
    });
    this.selectedArticle = this.articleService.article;
  }

  onArticleSelected(article) {
    this.selectedArticle = article;
  }

  onOpenArticle() {
    this.articleService.openArticle({_id: this.selectedArticle._id});
  }

}
