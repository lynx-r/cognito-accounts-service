import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BoardToolbarComponent} from './board-toolbar.component';
import {DraughtEditComponent} from "./draught-edit/draught-edit.component";
import {OpenArticleDialogComponent} from "../dialogs/open-article-dialog/open-article-dialog.component";
import {CreateArticleDialogComponent} from "../dialogs/create-article-dialog/create-article-dialog.component";
import {FormsModule} from "@angular/forms";
import {ArticleService} from "../../service/article.service";
import {BoardService} from "app/service/board.service";
import {SocketService} from "../../service/socket.service";
import {Ng2Webstorage} from "ngx-webstorage";
import {By} from "@angular/platform-browser";

describe('BoardToolbarComponent', () => {
  let component: BoardToolbarComponent;
  let fixture: ComponentFixture<BoardToolbarComponent>;
  let createArticleEl;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BoardToolbarComponent, DraughtEditComponent,
        OpenArticleDialogComponent, CreateArticleDialogComponent],
      imports: [FormsModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BoardToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    createArticleEl = fixture.debugElement.query(By.css('.create-article')); // find hero element
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  // it('should open create new dialog', (() => {
  //   // createArticleEl.triggerEventHandler('click', null);
  //   fixture.detectChanges();
  //   fixture.whenStable().then(() => {
  //     expect(fixture.debugElement.query(By.css('#createArticle')).query(By.css('.modal-title')).nativeElement.textContent).toBe('Создать статью');
  //   })
  //   // fixture.whenStable().then(() => {
  //   //   expect(element(By.css('#createArticle')).isDisplayed()).toBe(true);
  //   // })
  //   // let createButton = element('#create');
  //   // createButton.click();
  //
  //   // fixture.whenStable().then(() => {
  //   //   expect(element('#createArticle').element('.modal-title').getText()).toEqual('Создать статью');
  //   // })
  // }));
});
