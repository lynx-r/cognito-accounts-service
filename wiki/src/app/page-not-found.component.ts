import {Component} from '@angular/core';
import {SocketService} from "./service/socket.service";

@Component({
  template: `
    <h3>404 Страница не найдена</h3>
  `,
})
export class PageNotFoundComponent {

  constructor() {
  }
}
