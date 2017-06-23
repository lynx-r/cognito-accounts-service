import {Square} from "./square";
import {Draught} from "./draught";
import {EnumRules} from "./rules.enum";
export interface Board {
  _id: string;
  board: Square[];
  wDraughts: Draught[];
  bDraughts: Draught[];
  squareSize: number;
  rules: EnumRules;
  black: boolean;
}
