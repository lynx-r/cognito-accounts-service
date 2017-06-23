import { Component, OnInit } from '@angular/core';
import {ArticleService} from "../service/article.service";
import {Article} from "../model/article";
import {Observable} from "rxjs/Observable";

@Component({
  selector: 'app-articles',
  templateUrl: './articles.component.html',
  styleUrls: ['./articles.component.css']
})
export class ArticlesComponent implements OnInit {

  articles: Observable<Article[]>;

  constructor(private articleService: ArticleService) { }

  ngOnInit() {
    this.articles = this.articleService.findArticles();
  }

}
