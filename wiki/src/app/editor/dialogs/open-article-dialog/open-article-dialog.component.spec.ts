import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { OpenArticleDialogComponent } from './open-article-dialog.component';
import {FormsModule} from "@angular/forms";
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "../../../service/article.service";
import {BoardService} from "../../../service/board.service";
import {SocketService} from "../../../service/socket.service";

describe('OpenArticleDialogComponent', () => {
  let component: OpenArticleDialogComponent;
  let fixture: ComponentFixture<OpenArticleDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ OpenArticleDialogComponent ],
      imports: [FormsModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OpenArticleDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
