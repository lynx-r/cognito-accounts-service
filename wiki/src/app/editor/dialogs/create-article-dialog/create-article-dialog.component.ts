import {Component, Input, OnInit} from '@angular/core';
import {EnumRules} from "../../../model/rules.enum";
import {ArticleService} from "../../../service/article.service";
import {Article} from "../../../model/article";

@Component({
  selector: 'create-article-dialog',
  templateUrl: './create-article-dialog.component.html',
  styleUrls: ['./create-article-dialog.component.css']
})
export class CreateArticleDialogComponent implements OnInit {

  @Input() title: string;
  @Input() black: boolean = false;
  @Input() rules: EnumRules = EnumRules.RUSSIAN;
  EnumRules: typeof EnumRules = EnumRules;

  constructor(private articleService: ArticleService) { }

  ngOnInit() {
  }

  onNewArticle() {
    let config = {
      title: this.title,
      black: this.black,
      rules: this.rules,
      squareSize: this.getSquareSize()
    };
    this.articleService.createArticle(config);
  }

  getSquareSize(): number {
    return 60;
  }

}
