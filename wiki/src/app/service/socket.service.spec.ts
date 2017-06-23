import { TestBed, inject } from '@angular/core/testing';

import { SocketService } from './socket.service';
import {Ng2Webstorage} from "ngx-webstorage";
import {ArticleService} from "./article.service";
import {BoardService} from "./board.service";

describe('SocketService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],
    });
  });

  it('should ...', inject([SocketService], (service: SocketService) => {
    expect(service).toBeTruthy();
  }));
});
