import {Board} from "./board";
export interface Article {
  _id: string;
  title: string;
  article: string;
  author: string;
  board: Board;
}
