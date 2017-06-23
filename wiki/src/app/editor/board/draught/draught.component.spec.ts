import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DraughtComponent } from './draught.component';
import {ArticleService} from "../../../service/article.service";
import {BoardService} from "../../../service/board.service";
import {SocketService} from "../../../service/socket.service";
import {FormsModule} from "@angular/forms";
import {Ng2Webstorage} from "ngx-webstorage";

describe('DraughtComponent', () => {
  let component: DraughtComponent;
  let fixture: ComponentFixture<DraughtComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DraughtComponent ],
      imports: [FormsModule,
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DraughtComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
