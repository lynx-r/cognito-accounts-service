import { TestBed, inject } from '@angular/core/testing';

import { BoardService } from './board.service';
import {FormsModule} from "@angular/forms";
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "./article.service";
import {SocketService} from "./socket.service";

describe('BoardService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    });
  });

  it('should ...', inject([BoardService], (service: BoardService) => {
    expect(service).toBeTruthy();
  }));
});
