import { TestBed, inject } from '@angular/core/testing';

import { Utils } from './utils.service';

describe('Utils', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [Utils]
    });
  });

  it('should ...', inject([Utils], (service: Utils) => {
    expect(service).toBeTruthy();
  }));
});
