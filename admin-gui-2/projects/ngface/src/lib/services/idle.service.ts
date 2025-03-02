import {Injectable} from '@angular/core';
import {fromEvent, Subject} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class IdleService
{
  public refreshToken$: Subject<void> = new Subject();
  private refreshDisabled = false;

  constructor()
  {
    // Setup events
    fromEvent(document, 'click').subscribe(() => this.onInteraction());
    fromEvent(document, 'touchstart').subscribe(() => this.onInteraction());
    fromEvent(document, 'keydown').subscribe(() => this.onInteraction());
  }

  onInteraction(): void
  {
    if (!this.refreshDisabled)
    {
      this.refreshToken$.next();
      this.refreshDisabled = true;

      setTimeout(() =>
      {
        this.refreshDisabled = false;
      }, 30_000);
    }
  }
}
