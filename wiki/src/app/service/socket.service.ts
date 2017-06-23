import {Injectable} from '@angular/core';
import Socket = SocketIOClient.Socket;
import * as io from 'socket.io-client';
import ConnectOpts = SocketIOClient.ConnectOpts;

@Injectable()
export class SocketService {

  socket: Socket;
  connectOpts: ConnectOpts = {
    forceNew: true
  };

  constructor() {
  }

  connect() {
    // this.socket = io('http://localhost:4000', this.connectOpts);
  }

}
