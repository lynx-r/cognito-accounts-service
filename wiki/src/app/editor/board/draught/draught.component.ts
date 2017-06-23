import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Draught} from "../../../model/draught";
import {BoardService} from "../../../service/board.service";
import {Square} from "../../../model/square";
import {TweenLite, Linear} from 'gsap';
import {Utils} from "../../../service/utils.service";
import {LocalStorage} from "ngx-webstorage";
import {AppConstants} from "../../../service/app-constants";
import {Article} from "../../../model/article";

@Component({
  selector: 'draught',
  templateUrl: './draught.component.html',
  styleUrls: ['./draught.component.css']
})
export class DraughtComponent implements OnInit {

  @ViewChild('draughtRef') draughtRef: ElementRef;
  @Input() square: Square;
  @Input() playsBlack: boolean;
  draught: Draught;
  private allowedSquares: Square[];
  private beatenPos: Square[];
  private size: number;
  @LocalStorage(AppConstants.EDIT_MODE_STORAGE_KEY) editMode: boolean;
  @LocalStorage(AppConstants.ARTICLE_STORAGE_KEY) article: Article;

  constructor(private boardService: BoardService) {
  }

  ngOnInit() {
    if (this.square) {
      this.draught = this.square.draught;
      // берем 80% от размера клетки
      this.size = this.square.size - this.square.size / 10 * 2;

      if (this.draught.highlighted) {
        this.highlightAllowedFor(this.draught);
      }
    }
    this.boardService.squareClickedEvent.subscribe((square: Square) => {
      this.moveDraughtTo(square);
    });
    this.boardService.observableEditMode().subscribe((mode) => {
      if (mode) {
        this.highlightAllowedFor(null);
        this.draught.highlighted = false;
      }
    });
  }

  onDraughtClick() {
    if (!this.editMode && this.playsBlack == this.draught.black) {
      this.highlightAllowedFor(this.draught);
    }
  }

  private highlightAllowedFor(draught: Draught) {
    const config = {
      _articleId: this.article._id,
      draught: draught
    };
    this.boardService.highlightAllowedFor(config, (resp) => {
      if (draught) {
        draught.highlighted = true;
      }
      this.beatenPos = resp.data.highlighted.beaten;
      this.allowedSquares = <Square[]>resp.data.highlighted.allow;
    });
  }

  private moveDraughtTo(targetSquare: Square) {
    if (targetSquare.occupied && !targetSquare.draught.beaten
      || !this.draught.highlighted
      || !targetSquare.black
      || targetSquare == this.square
      || !this.allowedSquares
      || this.allowedSquares.length == 0) {
      return;
    }
    const config = {
      _articleId: this.article._id,
      allowedSquares: this.allowedSquares,
      beatenPos: this.beatenPos,
      square: targetSquare
    };
    this.boardService.moveDraughtTo(config, (resp) => {
      if (resp.ok) {
        const move = resp.data.move;
        TweenLite.to(this.draughtRef.nativeElement, .3, {
          css: {x: `${move.x}px`, y: `${move.y}px`},
          ease: Linear.easeNone, onComplete: () => {
            this.boardService.beatDraughts(this.beatenPos);
            this.boardService.moveDraught(this.square, this.draught, targetSquare, move.queen);
            this.allowedSquares = [];
            this.beatenPos = [];
            this.boardService.resetHighlight();
            this.article = resp.data.article;
          }
        });
      } else {
        Utils.handleError(resp.comment);
      }
    })
  }

}
