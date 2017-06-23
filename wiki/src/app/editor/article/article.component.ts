import { Component, OnInit } from '@angular/core';
import {ArticleService} from "../../service/article.service";
import {Article} from "../../model/article";
import {LocalStorage} from "ngx-webstorage";
import {AppConstants} from "../../service/app-constants";

@Component({
  selector: 'board-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.css']
})
export class ArticleComponent implements OnInit {
  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;

  constructor() { }

  ngOnInit() {
  }

}
