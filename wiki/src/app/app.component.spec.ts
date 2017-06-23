import {TestBed, async} from '@angular/core/testing';

import {AppComponent} from './app.component';
import {CreateArticleDialogComponent} from "./editor/dialogs/create-article-dialog/create-article-dialog.component";
import {OpenArticleDialogComponent} from "./editor/dialogs/open-article-dialog/open-article-dialog.component";
import {BrowserModule} from "@angular/platform-browser";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "./service/article.service";
import {BoardService} from "./service/board.service";
import {SocketService} from "./service/socket.service";
import {DraughtComponent} from "./editor/board/draught/draught.component";
import {BoardComponent} from "./editor/board/board.component";
import {SquareComponent} from "./editor/board/square/square.component";
import {BoardToolbarComponent} from "./editor/board-toolbar/board-toolbar.component";
import {DraughtEditComponent} from "./editor/board-toolbar/draught-edit/draught-edit.component";
import {HeaderComponent} from "./header/header.component";
import {FooterComponent} from "./footer/footer.component";
import {EditorComponent} from "./editor/editor.component";
import {DraughtMoveDirective} from "./directives/draught-move.directive";
import {ArticleComponent} from "./editor/article/article.component";
import {RouterTestingModule} from "@angular/router/testing";

describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AppComponent,
        DraughtComponent,
        BoardComponent,
        SquareComponent,
        BoardToolbarComponent,
        DraughtEditComponent,
        HeaderComponent,
        FooterComponent,
        EditorComponent,
        DraughtMoveDirective,
        ArticleComponent,
        CreateArticleDialogComponent,
        OpenArticleDialogComponent
      ],
      imports: [
        RouterTestingModule,
        BrowserModule,
        FormsModule,
        // HttpModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})
      ],
      providers: [ArticleService, BoardService, SocketService],
    }).compileComponents();
  }));

  it('should create the app', (() => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  }));

  // it('should render title in a h1 tag', async(() => {
  //   const fixture = TestBed.createComponent(AppComponent);
  //   fixture.detectChanges();
  //   const compiled = fixture.debugElement.nativeElement;
  //   expect(compiled.querySelector('app-header').textContent).toContain('Shashki Online');
  // }));
});
