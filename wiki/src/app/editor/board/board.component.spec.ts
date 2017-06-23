import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { BoardComponent } from './board.component';
import {SocketService} from "../../service/socket.service";
import {BoardService} from "../../service/board.service";
import {FormsModule} from "@angular/forms";
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "../../service/article.service";
import {SquareComponent} from "./square/square.component";
import {DraughtComponent} from "./draught/draught.component";

describe('BoardComponent', () => {
  let component: BoardComponent;
  let fixture: ComponentFixture<BoardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ BoardComponent,SquareComponent,DraughtComponent ],
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
    fixture = TestBed.createComponent(BoardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
