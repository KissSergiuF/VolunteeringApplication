import { TestBed } from '@angular/core/testing';

import { EventFeedbackService } from './event-feedback.service';

describe('EventFeedbackService', () => {
  let service: EventFeedbackService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EventFeedbackService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
