import { TestBed, inject } from '@angular/core/testing';

import { ArticleService } from './article.service';
import {Ng2Webstorage} from "ngx-webstorage";
import {BoardService} from "./board.service";
import {SocketService} from "./socket.service";

describe('ArticleService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        Ng2Webstorage,
        Ng2Webstorage.forRoot({prefix: 'shashki', separator: '.', caseSensitive: true})],
      providers: [ArticleService, BoardService, SocketService],

    });
  });

  it('should ...', inject([ArticleService], (service: ArticleService) => {
    expect(service).toBeTruthy();
  }));
});
