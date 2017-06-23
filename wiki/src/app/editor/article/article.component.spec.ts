import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ArticleComponent} from './article.component';
import {FormsModule} from "@angular/forms";
import {ArticleService} from "../../service/article.service";
import {BoardService} from "../../service/board.service";
import {SocketService} from "../../service/socket.service";
import {Ng2Webstorage} from "ngx-webstorage/dist/app";

describe('ArticleComponent', () => {
  let component: ArticleComponent;
  let fixture: ComponentFixture<ArticleComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ArticleComponent],
      imports: [
        FormsModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})
      ],
      providers: [ArticleService, BoardService, SocketService],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ArticleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
