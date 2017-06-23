import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MostRecentArticlesComponent } from './most-recent-articles.component';

describe('MostRecentArticlesComponent', () => {
  let component: MostRecentArticlesComponent;
  let fixture: ComponentFixture<MostRecentArticlesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MostRecentArticlesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MostRecentArticlesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
