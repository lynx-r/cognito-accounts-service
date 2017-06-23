import {Draught} from "./draught";
export interface Square {
  x: number;
  y: number;
  black: boolean;
  highlight: boolean;
  occupied: boolean;
  draught: Draught;
  size: number;
}
