import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'draught-edit',
  templateUrl: './draught-edit.component.html',
  styleUrls: ['./draught-edit.component.css']
})
export class DraughtEditComponent implements OnInit {

  @Input() selected: boolean;
  @Input() black: boolean;
  @Input() queen: boolean;
  size: number;

  constructor() { }

  ngOnInit() {
    this.size = 38;
  }

}
