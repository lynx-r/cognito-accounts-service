import { Component, OnInit } from '@angular/core';
import {LocalStorage} from "ngx-webstorage";
import {Article} from "../model/article";
import {AppConstants} from "../service/app-constants";

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.css']
})
export class EditorComponent implements OnInit {

  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;

  constructor() { }

  ngOnInit() {
  }

}
