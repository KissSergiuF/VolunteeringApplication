import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchivedEventsComponent } from './archived-events.component';

describe('ArchivedEventsComponent', () => {
  let component: ArchivedEventsComponent;
  let fixture: ComponentFixture<ArchivedEventsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ArchivedEventsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ArchivedEventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
