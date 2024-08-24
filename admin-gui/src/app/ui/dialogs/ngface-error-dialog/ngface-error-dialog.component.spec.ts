import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NgfaceErrorDialogComponent } from './ngface-error-dialog.component';

describe('NgfaceErrorDialogComponent', () => {
  let component: NgfaceErrorDialogComponent;
  let fixture: ComponentFixture<NgfaceErrorDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgfaceErrorDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NgfaceErrorDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
