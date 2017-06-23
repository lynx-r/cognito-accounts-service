import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DraughtEditComponent } from './draught-edit.component';

describe('DraughtEditComponent', () => {
  let component: DraughtEditComponent;
  let fixture: ComponentFixture<DraughtEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DraughtEditComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DraughtEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
