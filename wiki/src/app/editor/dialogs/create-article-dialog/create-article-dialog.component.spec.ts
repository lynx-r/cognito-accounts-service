import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateArticleDialogComponent } from './create-article-dialog.component';
import {FormsModule} from "@angular/forms";
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "../../../service/article.service";
import {BoardService} from "../../../service/board.service";
import {SocketService} from "../../../service/socket.service";

describe('CreateArticleDialogComponent', () => {
  let component: CreateArticleDialogComponent;
  let fixture: ComponentFixture<CreateArticleDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CreateArticleDialogComponent ],
      imports: [FormsModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateArticleDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
