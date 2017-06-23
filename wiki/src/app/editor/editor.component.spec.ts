import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditorComponent} from './editor.component';
import {BoardToolbarComponent} from "./board-toolbar/board-toolbar.component";
import {BoardComponent} from "./board/board.component";
import {ArticleComponent} from "./article/article.component";
import {OpenArticleDialogComponent} from "./dialogs/open-article-dialog/open-article-dialog.component";
import {CreateArticleDialogComponent} from "./dialogs/create-article-dialog/create-article-dialog.component";
import {DraughtEditComponent} from "./board-toolbar/draught-edit/draught-edit.component";
import {SquareComponent} from "./board/square/square.component";
import {DraughtComponent} from "./board/draught/draught.component";
import {FormsModule} from "@angular/forms";
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "../service/article.service";
import {BoardService} from "../service/board.service";
import {SocketService} from "../service/socket.service";

describe('EditorComponent', () => {
  let component: EditorComponent;
  let fixture: ComponentFixture<EditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EditorComponent, BoardToolbarComponent, BoardComponent, ArticleComponent,
        OpenArticleDialogComponent, CreateArticleDialogComponent, DraughtEditComponent, SquareComponent,
        DraughtComponent],
      imports: [FormsModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
